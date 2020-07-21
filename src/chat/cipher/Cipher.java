package chat.cipher;

import chat.Util;

import java.io.Serializable;


public class Cipher implements Serializable {
    private final byte[] encryptedInitialValue;
    private final byte[] encrypted;
    private final byte[] encryptedCounter;

    public Cipher(byte[] plain, byte[] symmetricKey, long counter) {
        byte[] initialValue = Util.getRandomByteArray(8);
        this.encryptedInitialValue = CipherUtil.ecbEncrypt(initialValue, symmetricKey);
        this.encrypted = CipherUtil.cfbEncrypt(plain, initialValue, symmetricKey);
        this.encryptedCounter = CipherUtil.ecbEncrypt(Util.longToBytes(counter), symmetricKey);
    }

    public byte[] decrypt(byte[] symmetricKey) {
        return CipherUtil.cfbDecrypt(encrypted,
                CipherUtil.ecbDecrypt(encryptedInitialValue, 8, symmetricKey), symmetricKey);
    }

    public long decryptCounter(byte[] symmetricKey) {
        return Util.bytesToLong(CipherUtil.ecbDecrypt(encryptedCounter, Long.BYTES, symmetricKey));
    }
}
