package se.tink.backend.aggregation.agents.nxgen.at.banks.ing.utils;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.spec.RSAPublicKeySpec;

public class IngAtRsa {
    private int e;
    private BigInteger n;
    private BigInteger d;
    private BigInteger p;
    private BigInteger q;
    private BigInteger dmp1;
    private BigInteger dmq1;
    private BigInteger coeff;
    private boolean canEncrypt;
    private boolean canDecrypt;

    public IngAtRsa(
            final BigInteger N,
            final int E,
            final BigInteger D,
            final BigInteger P,
            final BigInteger Q,
            final BigInteger Dmp1,
            final BigInteger Dmq1,
            final BigInteger Coeff) {
        this.n = N;
        this.e = E;
        this.d = D;
        this.p = P;
        this.q = Q;
        this.dmp1 = Dmp1;
        this.dmq1 = Dmq1;
        this.coeff = Coeff;
        this.canEncrypt = ((!(this.n == null)) && (!(this.e == 0)));
        this.canDecrypt = (canEncrypt && (!((this.d == null))));
    }

    public IngAtRsa(RSAPublicKeySpec rsaPublicKeySpec) {
        this(
                rsaPublicKeySpec.getModulus(),
                rsaPublicKeySpec.getPublicExponent().intValue(),
                null,
                null,
                null,
                null,
                null,
                null);
    }

    public static IngAtRsa parsePublicKey(final String n, final String e) {
        return (new IngAtRsa(
                new BigInteger(n, 16), Integer.parseInt(e), null, null, null, null, null, null));
    }

    public static IngAtRsa parsePrivateKey(
            final String n,
            final String e,
            final String d,
            final String p,
            final String q,
            final String Dmp1,
            final String Dmq1,
            final String Coeff) {
        if (p == null || p.equals("")) {
            return (new IngAtRsa(
                    new BigInteger(n, 16),
                    Integer.parseInt(e),
                    new BigInteger(d, 16),
                    null,
                    null,
                    null,
                    null,
                    null));
        }

        return (new IngAtRsa(
                new BigInteger(n, 16),
                Integer.parseInt(e),
                new BigInteger(d, 16),
                new BigInteger(p, 16),
                new BigInteger(q, 16),
                new BigInteger(Dmp1),
                new BigInteger(Dmq1),
                new BigInteger(Coeff)));
    }

    private static byte[] pkcs1unpad(final BigInteger src, final int n) {
        final byte[] b = src.toByteArray();

        int i = 0;
        while ((i < b.length) && (b[i] == 0)) {
            i++;
        }

        if (((b.length - i) != (n - 1)) || (b[i] != 0x2)) return null;

        i++;

        while (b[i] != 0) {
            if (++i >= b.length) return null;
        }
        final byte[] out = new byte[b.length - (i + 1)];
        int p = 0;
        while (++i < b.length) {
            out[p] = b[i];
            p++;
        }
        return out;
    }

    private int getBlockSize() {
        return (int) Math.floor((n.bitLength() + 7) / 8);
    }

    private int getBlockSizeBase(final int b) throws IllegalArgumentException {
        int k = 0;
        int adjustedBl = n.bitLength();
        switch (b) {
            case 2:
                k = 1;
                break;
            case 4:
                k = 2;
                break;
            case 16:
                k = 4;
                break;
            case 64:
                k = 6;
                adjustedBl = (int) (12 * Math.floor((n.bitLength() + 11) / 12));
                break;
            case 256:
                k = 8;
                break;
            default:
                throw new IllegalArgumentException("Invalid block size " + b);
        }

        return (int) (Math.floor(adjustedBl + k - 1) / k);
    }

    private void dispose() {
        e = 0;
        n = null;
    }

    public BigInteger doPublic(final BigInteger x) {
        if (this.canEncrypt) {
            return x.modPow(new BigInteger(this.e + ""), this.n);
        }

        return BigInteger.ZERO;
    }

    public String encrypt(final String text) throws IllegalArgumentException {
        final byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
        if (bytes.length > this.getBlockSize() - 11) {
            throw new IllegalArgumentException("The message is too big to be encrypted");
        }

        final BigInteger m = new BigInteger(this.pkcs1pad2(bytes, this.getBlockSize()));
        if (m.equals(BigInteger.ZERO)) {
            return null;
        }

        final BigInteger c = this.doPublic(m);
        if (c.equals(BigInteger.ZERO)) {
            return null;
        }

        final String result = c.toString(16);
        if ((result.length() & 1) == 0) {
            return result;
        }

        return "0" + result;
    }

    private byte[] pkcs1pad2(final byte[] data, int n) {
        byte[] bytes = new byte[n];
        int i = data.length - 1;
        while (i >= 0 && n > 11) {
            bytes[--n] = data[i--];
        }
        bytes[--n] = 0;

        while (n > 2) {
            bytes[--n] = 0x01; // random byte
        }

        bytes[--n] = 0x2;
        bytes[--n] = 0;

        return bytes;
    }

    public BigInteger doPrivate(final BigInteger x) {
        if (this.canDecrypt) {
            return x.modPow(this.d, this.n);
        }

        return BigInteger.ZERO;
    }

    public String decrypt(final String ctext) {
        return this.decrypt(ctext, 16);
    }

    public String decrypt(final String ctext, final int size) {
        final BigInteger c = new BigInteger(ctext, size);
        final BigInteger m = this.doPrivate(c);
        if (m.equals(BigInteger.ZERO)) {
            return null;
        }

        final byte[] bytes = pkcs1unpad(m, this.getBlockSize());

        if (bytes == null) {
            return null;
        }

        return new String(bytes, StandardCharsets.UTF_8);
    }
}
