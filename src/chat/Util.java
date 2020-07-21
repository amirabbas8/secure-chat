package chat;

import java.io.PrintStream;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.Random;


public class Util {

    public static void indent(PrintStream out, int levels) {
        for (int i = 0; i < levels; ++i) out.print("  ");
    }

    private static final Random rand = new SecureRandom();

    public static byte[] getRandomByteArray(int num) {
        byte[] ret = new byte[num];
        rand.nextBytes(ret);
        return ret;
    }

    public static int getRandomInt() {
        return rand.nextInt();
    }

    public static byte[] longToBytes(long x) {
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.putLong(x);
        return buffer.array();
    }

    public static long bytesToLong(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.put(bytes);
        buffer.flip();//need flip
        return buffer.getLong();
    }
}
