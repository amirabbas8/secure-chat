import java.io.Serializable;

public class Cipher implements Serializable {
    public final int originalLength;
    public final byte[] encrypted;

    Cipher(byte[] plain) {
        this.originalLength = plain.length;
        this.encrypted = Util.encrypt(plain);
    }
}
