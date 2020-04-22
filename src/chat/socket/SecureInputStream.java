package chat.socket;

import chat.cipher.Cipher;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;

public class SecureInputStream {
    private InputStream inputStream;
    private byte[] symmetricKey;

    SecureInputStream(InputStream inputStream, byte[] symmetricKey) {
        this.inputStream = inputStream;
        this.symmetricKey = symmetricKey;
    }

    public Integer read() throws IOException {
        byte[] b = readBytes();
        if (b == null) return null;
        return (int) b[0];
    }

    public byte[] readBytes() throws IOException {
        ObjectInputStream ois = new ObjectInputStream(inputStream);
        try {
            Cipher cipher = (Cipher) ois.readObject();
            return cipher.decrypt(symmetricKey);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void close() throws IOException {
        inputStream.close();
    }
}
