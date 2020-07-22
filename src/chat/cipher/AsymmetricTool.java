package chat.cipher;

import java.math.BigInteger;

public class AsymmetricTool {


    public static byte[] encrypt(AsymmetricKey key, byte[] input) {
        return new BigInteger(input).modPow(key.pow, key.mod).toByteArray();
    }

    public static byte[] decrypt(AsymmetricKey key, byte[] input) {
        return new BigInteger(input).modPow(key.pow, key.mod).toByteArray();
    }
}
