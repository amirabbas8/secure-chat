package chat.cipher;

class CipherUtil {

    private static byte[] padBytes(byte[] bytes) {
        int padLength = bytes.length % 8 != 0 ? (bytes.length / 8 + 1) * 8 - bytes.length : 0;
        byte[] paddedBytes = new byte[bytes.length + padLength];
        System.arraycopy(bytes, 0, paddedBytes, 0, bytes.length);
        System.arraycopy(new byte[padLength], 0, paddedBytes, bytes.length, padLength);
        return paddedBytes;
    }

    private static byte[] slice(byte[] paddedBytes, int cutLength) {
        byte[] bytes = new byte[cutLength];
        System.arraycopy(paddedBytes, 0, bytes, 0, cutLength);
        return bytes;
    }

    static byte[] ecbEncrypt(byte[] plain, byte[] symmetricKey) {
        byte[] paddedPlain = padBytes(plain);
        BlockCipher blockCipher = new BlockCipher(symmetricKey);
        byte[] cipher = new byte[paddedPlain.length];
        for (int i = 0; i < paddedPlain.length / 8; i++) {
            blockCipher.encrypt(paddedPlain, i * 8, cipher, i * 8);
        }
        return cipher;
    }

    static byte[] ecbDecrypt(byte[] encrypted, int originalLength, byte[] symmetricKey) {
        BlockCipher blockCipher = new BlockCipher(symmetricKey);
        byte[] plain = new byte[encrypted.length];
        for (int i = 0; i < encrypted.length / 8; i++) {
            blockCipher.decrypt(encrypted, i * 8, plain, i * 8);
        }
        return CipherUtil.slice(plain, originalLength);
    }

    // iv is 8 byte
    protected static byte[] cfbEncrypt(byte[] plain, byte[] initialValue, byte[] symmetricKey) {
        byte[] esr = new byte[8];
        byte[] cipher = new byte[plain.length];
        BlockCipher blockCipher = new BlockCipher(symmetricKey);
        for (int i = 0; i < plain.length; i++) {
            blockCipher.encrypt(initialValue, 0, esr, 0);
            cipher[i] = (byte) (plain[i] ^ esr[7]);
            System.arraycopy(initialValue, 1, initialValue, 0, 7);
            initialValue[7] = cipher[i];
        }
        return cipher;
    }

    protected static byte[] cfbDecrypt(byte[] cipher, byte[] initialValue, byte[] symmetricKey) {
        byte[] esr = new byte[8];
        byte[] plain = new byte[cipher.length];
        BlockCipher blockCipher = new BlockCipher(symmetricKey);
        for (int i = 0; i < cipher.length; i++) {
            blockCipher.encrypt(initialValue, 0, esr, 0);
            plain[i] = (byte) (cipher[i] ^ esr[7]);
            System.arraycopy(initialValue, 1, initialValue, 0, 7);
            initialValue[7] = cipher[i];
        }
        return plain;
    }
}
