package chat.socket;

import chat.cipher.AsymmetricKey;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;


public class SecureServerSocket {
    private ServerSocket ss;
    private final AsymmetricKey privateKey;

    public SecureServerSocket(int portNum, AsymmetricKey myPrivateKey)
            throws IOException {
        ss = new ServerSocket(portNum);
        privateKey = myPrivateKey;
    }

    public SecureSocket accept() throws IOException {
        Socket sock = ss.accept();
        return new SecureSocket(sock, privateKey);
    }
}
