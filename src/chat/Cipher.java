package chat;

import java.io.Serializable;

public class Cipher implements Serializable {
    public final byte[] encryptedInitialValue;
    public final byte[] encrypted;

    Cipher(byte[] plain) {
        byte[] initialValue = Util.getRandomByteArray(8);
        this.encryptedInitialValue = CipherUtil.ecbEncrypt(initialValue);
        this.encrypted = CipherUtil.cfbEncrypt(plain, initialValue);
    }
}
