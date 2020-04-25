package chat;

import chat.auth.AuthenticationInfo;
import chat.socket.SecureInputStream;
import chat.socket.SecureOutputStream;
import chat.socket.SecureSocket;

import java.io.IOException;
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

        SecureOutputStream out = sock.getOutputStream();
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
        captcha();
        try {
            new ChatClient(username, hostname, ChatServer.portNum, null, null);
        } catch (IOException x) {
            x.printStackTrace();
        }
    }

    private static void captcha() {
        int n1 = Util.getRandomInt() % 10;
        int n2 = Util.getRandomInt() % 10;
        System.out.println("what is the sum of " + n1 + " + " + n2);
        Scanner scanner = new Scanner(System.in);
        int c = scanner.nextInt();
        if (c != n1 + n2) {
            System.exit(1);
        }

    }

    private boolean sendAuth(String username, String password, SecureSocket socket) throws IOException {
        // create an AuthInfo object to authenticate the local user,
        // and send the AuthInfo to the server

        byte[] serverNonce = getRemoteNonce(socket);
        byte[] clientNonce = Util.getRandomByteArray(8);
        AuthenticationInfo auth = new AuthenticationInfo(username, password, serverNonce, clientNonce);
        socket.getOutputStream().write(auth.serialize());
        if (!Arrays.equals(getRemoteNonce(socket), clientNonce)) {
            System.out.println("Authentication failed!");
            return false;
        }
        System.out.println("Authentication successful!");
        return true;
    }

    private byte[] getRemoteNonce(SecureSocket socket) throws IOException {
        byte[] remoteNonce = socket.getInputStream().readBytes();
        if (remoteNonce == null) {
            System.out.println("Server closed the connection!");
            System.exit(1);
        }
        return remoteNonce;
    }

    private static class ReceiverThread extends Thread {
        // gather incoming messages, and display them

        private SecureInputStream in;

        ReceiverThread(SecureInputStream inStream) {
            in = inStream;
            start();
        }

        @SuppressWarnings("InfiniteLoopStatement")
        public void run() {
            try {
                for (; ; ) System.out.print(new String(in.readBytes()));
            } catch (IOException ignored) {
                System.out.println("Server closed the connection!");
                System.exit(1);
            }
        }
    }
}
