import java.io.Serializable;


public class AuthenticationInfo implements Serializable {
    // The fields of this object are set by the client, and used by the
    // server to validate the client's identity.  The client constructs this
    // object (by calling the constructor).  The client software (in another
    // source code file) then sends the object across to the server.  Finally,
    // the server verifies the object by calling isValid().

    private String username;

    public AuthenticationInfo(String name) {
        // This is called by the client to initialize the object.

        username = name;
    }

    public boolean isValid() {
        // This is called by the server to make sure the user is who he/she
        // claims to be.

        // Presently, this is totally insecure -- the server just accepts the
        // client's assertion without checking anything.  Homework assignment 1
        // is to make this more secure.

        return true;
    }

    public String getUserName() {
        return isValid() ? username : null;
    }
}
