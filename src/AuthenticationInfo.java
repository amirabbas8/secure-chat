import java.io.Serializable;
import java.util.Arrays;


public class AuthenticationInfo implements Serializable {
    // The fields of this object are set by the client, and used by the
    // server to validate the client's identity.  The client constructs this
    // object (by calling the constructor).  The client software (in another
    // source code file) then sends the object across to the server.  Finally,
    // the server verifies the object by calling isValid().

    private String username;
    private byte[] hash;
    private byte[] clientNonce;

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

    public boolean isValid(byte[] serverNonce) {
        // This is called by the server to make sure the user is who he/she
        // claims to be.

        // Presently, this is totally insecure -- the server just accepts the
        // client's assertion without checking anything.  Homework assignment 1
        // is to make this more secure.
        HashFunction hashFunction = new HashFunction();
        hashFunction.update("pass".getBytes());
        byte[] hashedPass = hashFunction.digest();
        byte[] c = new byte[serverNonce.length + hashedPass.length];
        System.arraycopy(serverNonce, 0, c, 0, serverNonce.length);
        System.arraycopy(hashedPass, 0, c, serverNonce.length, hashedPass.length);
        hashFunction.update(c);
        return Arrays.equals(hashFunction.digest(), hash);
    }

    public AuthenticationInfo checked(byte[] serverNonce) {
        return isValid(serverNonce) ? this : null;
    }

    public String getUsername() {
        return username;
    }

    public byte[] getEncryptedClientNonce() {
//        todo encrypt
        return clientNonce;
    }
}
