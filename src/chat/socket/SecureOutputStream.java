package chat.socket;

import chat.cipher.Cipher;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

public class SecureOutputStream {
    private final OutputStream outputStream;
    private final byte[] symmetricKey;
    private long lastSentCipher = 0;

    SecureOutputStream(OutputStream outputStream, byte[] symmetricKey) {
        this.outputStream = outputStream;
        this.symmetricKey = symmetricKey;
    }

    public void write(int var1) throws IOException {
        byte[] b = new byte[1];
        b[0] = (byte) var1;
        this.write(b);
    }

    public void write(byte[] var1) throws IOException {
        ObjectOutputStream oos = new ObjectOutputStream(outputStream);
        oos.writeObject(new Cipher(var1, symmetricKey, ++lastSentCipher));
        oos.flush();
    }

    public void flush() throws IOException {
        outputStream.flush();
    }

    public void close() throws IOException {
        outputStream.close();
    }
}
