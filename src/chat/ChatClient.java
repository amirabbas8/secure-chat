package chat;

import java.io.*;
import java.util.Arrays;
import java.util.Scanner;


public class ChatClient {
    public ChatClient(String username, String serverHost, int serverPort,
                      byte[] clientPrivateKey, byte[] serverPublicKey) throws IOException {

        Scanner scanner = new Scanner(System.in);
        SecureSocket sock = null;

        String password;
        do {
            if (sock != null) {
                sock.close();
            }
            sock = new SecureSocket(serverHost, serverPort, clientPrivateKey, serverPublicKey);
            System.out.println("Give me password of " + username + ":");
            password = scanner.nextLine();
        } while (!sendAuth(username, password, sock));

        new ReceiverThread(sock.getInputStream());

        OutputStream out = sock.getOutputStream();
        for (; ; ) {
            int c = System.in.read();
            if (c == -1) break;
            out.write(c);
            if (c == '\n') out.flush();
        }
        sock.close();
    }

    public static void main(String[] argv) {
        String username = argv[0];
        String hostname = (argv.length <= 1) ? "localhost" : argv[1];
        try {
            new ChatClient(username, hostname, ChatServer.portNum, null, null);
        } catch (IOException x) {
            x.printStackTrace();
        }
    }

    private boolean sendAuth(String username, String password, SecureSocket socket) throws IOException {
        // create an AuthInfo object to authenticate the local user,
        // and send the AuthInfo to the server

        byte[] serverNonce = getRemoteNonce(socket);
        byte[] clientNonce = Util.getRandomByteArray(8);
        AuthenticationInfo auth = new AuthenticationInfo(username, password, serverNonce, clientNonce);
        ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
        oos.writeObject(new Cipher(auth.serialize()));
        oos.flush();
        if (!Arrays.equals(CipherUtil.ecbDecrypt(getRemoteNonce(socket), clientNonce.length), clientNonce)) {
            System.out.println("Authentication failed!");
            return false;
        }
        System.out.println("Authentication successful!");
        return true;
    }

    private byte[] getRemoteNonce(SecureSocket socket) throws IOException {
        byte[] remoteNonce = new byte[8];
        int c = socket.getInputStream().read(remoteNonce);
        if (c == -1) {
            System.out.println("Server closed the connection!");
            System.exit(1);
        }
        return remoteNonce;
    }

    class ReceiverThread extends Thread {
        // gather incoming messages, and display them

        private InputStream in;

        ReceiverThread(InputStream inStream) {
            in = inStream;
            start();
        }

        public void run() {
            try {
                ByteArrayOutputStream baos;  // queues up stuff until carriage-return
                baos = new ByteArrayOutputStream();
                for (; ; ) {
                    int c = in.read();
                    if (c == -1) {
                        spew(baos);
                        System.out.println("Server closed the connection!");
                        System.exit(1);
                    }
                    baos.write(c);
                    if (c == '\n') spew(baos);
                }
            } catch (IOException ignored) {
            }
        }

        private void spew(ByteArrayOutputStream baos) throws IOException {
            byte[] message = baos.toByteArray();
            baos.reset();
            System.out.write(message);
        }
    }
}
