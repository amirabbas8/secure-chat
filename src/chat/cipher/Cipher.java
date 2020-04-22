package chat.cipher;

import chat.Util;

import java.io.Serializable;

public class Cipher implements Serializable {
    private final byte[] encryptedInitialValue;
    private final byte[] encrypted;

    public Cipher(byte[] plain, byte[] symmetricKey) {
        byte[] initialValue = Util.getRandomByteArray(8);
        this.encryptedInitialValue = CipherUtil.ecbEncrypt(initialValue, symmetricKey);
        this.encrypted = CipherUtil.cfbEncrypt(plain, initialValue, symmetricKey);
    }

    public byte[] decrypt(byte[] symmetricKey) {
        return CipherUtil.cfbDecrypt(encrypted,
                CipherUtil.ecbDecrypt(encryptedInitialValue, 8, symmetricKey), symmetricKey);
    }
}
