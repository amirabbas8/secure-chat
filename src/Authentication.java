import java.util.Arrays;

public class Authentication {
    private static boolean isValid(AuthenticationInfo autInfo, byte[] serverNonce) {
        byte[] hashedPass = getUserPass();
        byte[] c = new byte[serverNonce.length + hashedPass.length];
        System.arraycopy(serverNonce, 0, c, 0, serverNonce.length);
        System.arraycopy(hashedPass, 0, c, serverNonce.length, hashedPass.length);

        HashFunction hashFunction = new HashFunction();
        hashFunction.update(c);
        return Arrays.equals(hashFunction.digest(), autInfo.hash);
    }

    private static byte[] getUserPass() {
//        todo fetch from db with username
        HashFunction hashFunction = new HashFunction();
        hashFunction.update("pass".getBytes());
        return hashFunction.digest();
    }

    // the server verifies the AuthenticationInfo by calling validate().
    public static AuthenticationInfo validate(AuthenticationInfo autInfo, byte[] serverNonce) {
        return isValid(autInfo, serverNonce) ? autInfo : null;
    }
}
