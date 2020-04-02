import java.io.*;


public class AuthenticationInfo implements Serializable {
    // The fields of this object are set by the client, and used by the
    // server to validate the client's identity.  The client constructs this
    // object (by calling the constructor).  The client software (in another
    // source code file) then sends the object across to the server.
    public final String username;
    public final byte[] hash;
    public final byte[] clientNonce;

    public AuthenticationInfo(String username, String password, byte[] serverNonce, byte[] clientNonce) {
        // This is called by the client to initialize the object.

        this.username = username;

        HashFunction hashFunction = new HashFunction();
        hashFunction.update(password.getBytes());
        byte[] hashedPass = hashFunction.digest();
        byte[] c = new byte[serverNonce.length + hashedPass.length];
        System.arraycopy(serverNonce, 0, c, 0, serverNonce.length);
        System.arraycopy(hashedPass, 0, c, serverNonce.length, hashedPass.length);
        hashFunction.update(c);
        this.hash = hashFunction.digest();

        this.clientNonce = clientNonce;
    }

    public byte[] serialize() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ObjectOutputStream os = new ObjectOutputStream(out);
        os.writeObject(this);
        return out.toByteArray();
    }

    public static AuthenticationInfo deserialize(byte[] data) throws IOException, ClassNotFoundException {
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        ObjectInputStream is = new ObjectInputStream(in);
        return (AuthenticationInfo) is.readObject();
    }
}
