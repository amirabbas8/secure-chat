package chat;

import chat.auth.Authentication;
import chat.auth.AuthenticationInfo;
import chat.cipher.AsymmetricKey;
import chat.socket.SecureInputStream;
import chat.socket.SecureOutputStream;
import chat.socket.SecureServerSocket;
import chat.socket.SecureSocket;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;


public class ChatServer {
    public static final int portNum = Config.getAsInt("ServerPortNum");

    private final Set<SenderThread> activeSenders = Collections.synchronizedSet(new HashSet<>());

    @SuppressWarnings("InfiniteLoopStatement")
    public ChatServer(AsymmetricKey myPrivateKey) {
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

        String d = (argv.length <= 1) ? "81419981902504204935199629019932340340482084307556048382309884454562935990142414843907009451691606759336348789566274183618912646996274524822680570689140732342541318325951719751445999302790336205157204299294867186171866821614143006056437193116790151906355744595812014448156142299051405751461258198122347076547"
                : argv[1];
        AsymmetricKey asymmetricKey = new AsymmetricKey();
        asymmetricKey.pow = new BigInteger(d);
        asymmetricKey.mod = new BigInteger("122129972853756307402799443529898510510723126461334072573464826681844403985213622265860514177537410139004523184349411275428368970494411787234020856033711120848505718712633607032435671600830370571475207661517999263285392877936557248038119310191041798633281647056365020935589355717404894715402988192847646619097");
        new ChatServer(asymmetricKey);
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

        private final SecureOutputStream out;
        private final Queue queue;

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

        private final SecureInputStream in;
        private final SenderThread me;
        private final byte[] userNameBytes;

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
