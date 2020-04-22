package chat;

import chat.cipher.HashFunction;
import chat.db.SQLiteJDBC;

import java.util.Arrays;

public class Authentication {
    private static boolean isValid(AuthenticationInfo autInfo, byte[] serverNonce) {
        byte[] hashedPass = getUserHash(autInfo.username);
        byte[] c = new byte[serverNonce.length + hashedPass.length];
        System.arraycopy(serverNonce, 0, c, 0, serverNonce.length);
        System.arraycopy(hashedPass, 0, c, serverNonce.length, hashedPass.length);

        HashFunction hashFunction = new HashFunction();
        hashFunction.update(c);
        return Arrays.equals(hashFunction.digest(), autInfo.hash);
    }

    private static byte[] getUserHash(String username) {
        SQLiteJDBC db = new SQLiteJDBC();
        byte[] hash = db.getUserHash(username);
        db.close();
        return hash;
    }

    // the server verifies the AuthenticationInfo by calling validate().
    public static AuthenticationInfo validate(AuthenticationInfo autInfo, byte[] serverNonce) {
        return isValid(autInfo, serverNonce) ? autInfo : null;
    }
}
