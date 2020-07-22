package chat.cipher;

import java.math.BigInteger;
import java.security.SecureRandom;

public class RSAGenerator {
    private BigInteger n, d, e;

    private final int len;

    public RSAGenerator(int bits) {
        len = bits;
        generateKeys();
    }

    public synchronized void generateKeys() {
        SecureRandom r = new SecureRandom();
        BigInteger p = new BigInteger(len / 2, 100, r);
        BigInteger q = new BigInteger(len / 2, 100, r);
        n = p.multiply(q);
        BigInteger phi = (p.subtract(BigInteger.ONE)).multiply(q.subtract(BigInteger.ONE));
        e = new BigInteger("3");
        while (phi.gcd(e).intValue() > 1) {
            e = e.add(new BigInteger("2"));
        }
        d = e.modInverse(phi);
    }

    public static void main(String[] args) {
        RSAGenerator rsa = new RSAGenerator(512);
        System.out.println("KEY Generated:");
        System.out.println("n mod: " + rsa.n);
        System.out.println("d private key: " + rsa.d);
        System.out.println("e public key: " + rsa.e);
    }
}