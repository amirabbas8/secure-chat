package chat;

import chat.auth.Authentication;
import chat.auth.AuthenticationInfo;
import chat.socket.SecureInputStream;
import chat.socket.SecureOutputStream;
import chat.socket.SecureServerSocket;
import chat.socket.SecureSocket;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;


public class ChatServer {
    public static final int portNum = Config.getAsInt("ServerPortNum");

    private Set<SenderThread> activeSenders = Collections.synchronizedSet(new HashSet<>());

    @SuppressWarnings("InfiniteLoopStatement")
    public ChatServer(byte[] myPrivateKey) {
        // This constructor never returns.captcha
        try {
            SecureServerSocket ss;
            System.out.println("I am listening on " + portNum);
            ss = new SecureServerSocket(portNum, myPrivateKey);
            for (; ; ) {
                // wait for a new client to connect, then hook it up properly
                SecureSocket sock = ss.accept();
                SecureInputStream in = sock.getInputStream();
                SecureOutputStream out = sock.getOutputStream();

                byte[] serverNonce = Util.getRandomByteArray(8);
                out.write(serverNonce);
                out.flush();
                AuthenticationInfo auth = getAuth(in, serverNonce);
                if (auth != null) {
                    System.err.println("Got connection from " + auth.username);
                    out.write(auth.clientNonce);
                    out.flush();
                    SenderThread st = new SenderThread(out);
                    new ReceiverThread(in, st, auth.username);
                } else {
                    sock.close();
                }
            }
        } catch (IOException x) {
            System.err.println("Dying: IOException");
        }
    }

    public static void main(String[] argv) {
        new ChatServer(null);
    }

    private AuthenticationInfo getAuth(SecureInputStream in, byte[] serverNonce) throws IOException {
        try {
            byte[] authInfoBytes = in.readBytes();
            AuthenticationInfo auth = AuthenticationInfo.deserialize(authInfoBytes);
            return Authentication.validate(auth, serverNonce);
        } catch (ClassNotFoundException x) {
            x.printStackTrace();
            return null;
        }
    }

    class SenderThread extends Thread {
        // forwards messages to a client
        // messages are queued
        // we take them from the queue and send them along

        private SecureOutputStream out;
        private Queue queue;

        SenderThread(SecureOutputStream outStream) {
            out = outStream;
            queue = new Queue();
            activeSenders.add(this);
            start();
        }

        public void queueForSending(byte[] message) {
            // queue a message, to be sent as soon as possible

            queue.put(message);
        }

        @SuppressWarnings("InfiniteLoopStatement")
        public void run() {
            // suck messages out of the queue and send them out
            try {
                for (; ; ) {
                    Object o = queue.get();
                    byte[] barr = (byte[]) o;
                    out.write(barr);
                    out.flush();
                }
            } catch (IOException x) {
                // unexpected exception -- stop relaying messages
                x.printStackTrace();
                try {
                    out.close();
                } catch (IOException ignored) {
                }
            }
            activeSenders.remove(this);
        }
    }

    class ReceiverThread extends Thread {
        // receives messages from a client, and forwards them to everybody else

        private SecureInputStream in;
        private SenderThread me;
        private byte[] userNameBytes;

        ReceiverThread(SecureInputStream inStream, SenderThread mySenderThread,
                       String name) {

            in = inStream;
            me = mySenderThread;
            String augmentedName = "[" + name + "] ";
            userNameBytes = augmentedName.getBytes();
            start();
        }

        public void run() {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            for (; ; ) {
                // read in a message, terminated by carriage-return
                // buffer the message in baos, until we see EOF or carriage-return
                // then send it out to all the other clients
                try {
                    baos.write(userNameBytes);
                    Integer c;
                    do {
                        c = in.read();
                        if (c == null) {
                            // got EOF -- send what we have, then quit
                            sendToOthers(baos);
                            return;
                        }
                        baos.write(c);
                    } while (c != '\n');
                    sendToOthers(baos);
                } catch (IOException x) {
                    // send what we have, then quit
                    sendToOthers(baos);
                    return;
                }
            }
        }

        private final SenderThread[] stArr = new SenderThread[1];

        private void sendToOthers(ByteArrayOutputStream baos) {
            // extract the contents of baos, and queue them for sending to all
            // other clients;
            // also, reset baos so it is empty and can be reused

            byte[] message = baos.toByteArray();
            baos.reset();
            System.out.print(new String(message));
            SenderThread[] guys = activeSenders.toArray(stArr);
            for (SenderThread st : guys) {
                if (st != me) st.queueForSending(message);
            }
        }
    }
}
