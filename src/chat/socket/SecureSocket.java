package chat.socket;// This file implements a secure (encrypted) version of the Socket class.
// (Actually, it is insecure as written, and students will fix the insecurities
// as part of their homework.)
//
// This class is meant to work in tandem with the SecureServerSocket class.
// The idea is that if you have a program that uses java.net.Socket and
// java.net.ServerSocket, you can make that program secure by replacing 
// java.net.Socket by this class, and java.net.ServerSocket by 
// SecureServerSocket.
//
// Like the ordinary Socket interface, this one differentiates between the
// client and server sides of a connection.  A server waits for connections
// on a SecureServerSocket, and a client uses this class to connect to a 
// server.
// 
// A client makes a connection like this:
//        String          serverHostname = ...
//        int             serverPort = ...
//        byte[]          myPrivateKey = ...
//        byte[]          serverPublicKey = ...
//        SecureSocket sock;
//        sock = new SecureSocket(serverHostname, serverPort,
//                                   myPrivateKey, serverPublicKey);
// 
// The keys are in a key-exchange protocol (which students will write), to
// establish a shared secret key that both the client and server know.
//
// Having created a SecureSocket, a program can get an associated
// InputStream (for receiving data that arrives on the socket) and an
// associated OutputStream (for sending data on the socket):
//
//         InputStream inStream = sock.getInputStream();
//         OutputStream outStream = sock.getOutputStream();


import chat.Util;
import chat.cipher.AsymmetricKey;
import chat.cipher.AsymmetricTool;
import chat.cipher.HashFunction;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Arrays;


public class SecureSocket {
    private final Socket sock;
    private SecureInputStream in;
    private SecureOutputStream out;

    public SecureSocket(String hostname, int port, AsymmetricKey serverPublicKey)
            throws IOException {
        // this constructor is called by a client who wants to make a secure
        // socket connection to a server

        sock = new Socket(hostname, port);

        byte[] symmetricKey = clientKeyExchange(serverPublicKey);

        setupStreams(symmetricKey);
    }

    public SecureSocket(Socket s, AsymmetricKey serverPrivateKey) throws IOException {
        // don't call this yourself
        // this is meant to be called by SecureServerSocket

        sock = s;

        byte[] symmetricKey = serverKeyExchange(serverPrivateKey);

        setupStreams(symmetricKey);
    }

    private byte[] serverKeyExchange(AsymmetricKey serverPrivateKey) throws IOException {
        InputStream instream = sock.getInputStream();
        byte[] inbytes = new byte[1000000];
        int num = instream.read(inbytes);
        if (num <= 0) throw new RuntimeException();
        inbytes = Arrays.copyOfRange(inbytes, 0, num);

        HashFunction hash = new HashFunction();
        hash.update(AsymmetricTool.decrypt(serverPrivateKey, inbytes));
        return hash.digest();
    }

    private byte[] clientKeyExchange(AsymmetricKey serverPublicKey) throws IOException {
        OutputStream outstream = sock.getOutputStream();
        byte[] outbytes = Util.getRandomByteArray(128);
        for (int i = 0; i < outbytes.length; i++) {
            outbytes[i] = (byte) Math.abs(outbytes[i]);
        }
        outstream.write(AsymmetricTool.encrypt(serverPublicKey, outbytes));
        outstream.flush();

        HashFunction hash = new HashFunction();
        hash.update(outbytes);
        return hash.digest();
    }

    private void setupStreams(byte[] symmetricKey) throws IOException {
        in = new SecureInputStream(sock.getInputStream(), symmetricKey);
        out = new SecureOutputStream(sock.getOutputStream(), symmetricKey);
    }

    public SecureInputStream getInputStream() {
        return in;
    }

    public SecureOutputStream getOutputStream() {
        return out;
    }

    public void close() throws IOException {
        in.close();
        out.close();
        sock.close();
    }
}
