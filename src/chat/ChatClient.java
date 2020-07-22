package chat;

import chat.auth.AuthenticationInfo;
import chat.cipher.AsymmetricKey;
import chat.socket.SecureInputStream;
import chat.socket.SecureOutputStream;
import chat.socket.SecureSocket;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Scanner;


public class ChatClient {
    public ChatClient(String username, String serverHost, int serverPort,
                      AsymmetricKey serverPublicKey) throws IOException {

        Scanner scanner = new Scanner(System.in);
        SecureSocket sock = null;

        String password;
        do {
            if (sock != null) {
                sock.close();
            }
            sock = new SecureSocket(serverHost, serverPort, serverPublicKey);
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
            AsymmetricKey asymmetricKey = new AsymmetricKey();
            asymmetricKey.pow = new BigInteger("3");
            asymmetricKey.mod = new BigInteger("122129972853756307402799443529898510510723126461334072573464826681844403985213622265860514177537410139004523184349411275428368970494411787234020856033711120848505718712633607032435671600830370571475207661517999263285392877936557248038119310191041798633281647056365020935589355717404894715402988192847646619097");
            new ChatClient(username, hostname, ChatServer.portNum, asymmetricKey);
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

        private final SecureInputStream in;

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
