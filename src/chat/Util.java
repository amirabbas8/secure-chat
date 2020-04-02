package chat;

import java.io.PrintStream;
import java.security.SecureRandom;
import java.util.Random;


public class Util {
    private static final byte[] KEY = Config.getAsString("ServerPortNum").getBytes();

    public static void indent(PrintStream out, int levels) {
        for (int i = 0; i < levels; ++i) out.print("  ");
    }

    private static final Random rand = new SecureRandom();

    public static byte[] getRandomByteArray(int num) {
        byte[] ret = new byte[num];
        rand.nextBytes(ret);
        return ret;
    }

    private static byte[] padBytes(byte[] bytes) {
        int padLength = bytes.length % 8 != 0 ? (bytes.length / 8 + 1) * 8 - bytes.length : 0;
        byte[] paddedBytes = new byte[bytes.length + padLength];
        System.arraycopy(bytes, 0, paddedBytes, 0, bytes.length);
        System.arraycopy(new byte[padLength], 0, paddedBytes, bytes.length, padLength);
        return paddedBytes;
    }

    public static byte[] slice(byte[] paddedBytes, int cutLength) {
        byte[] bytes = new byte[cutLength];
        System.arraycopy(paddedBytes, 0, bytes, 0, cutLength);
        return bytes;
    }

    public static byte[] encrypt(byte[] plain) {
        byte[] paddedPlain = padBytes(plain);
        BlockCipher blockCipher = new BlockCipher(KEY);
        byte[] cipher = new byte[paddedPlain.length];
        for (int i = 0; i < paddedPlain.length / 8; i++) {
            blockCipher.encrypt(paddedPlain, i * 8, cipher, i * 8);
        }
        return cipher;
    }

    public static byte[] decrypt(byte[] encrypted, int originalLength) {
        BlockCipher blockCipher = new BlockCipher(KEY);
        byte[] plain = new byte[encrypted.length];
        for (int i = 0; i < encrypted.length / 8; i++) {
            blockCipher.decrypt(encrypted, i * 8, plain, i * 8);
        }
        return Util.slice(plain, originalLength);
    }
}
