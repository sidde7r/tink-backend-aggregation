package se.tink.backend.aggregation.agents.nxgen.at.banks.ing.utils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.security.spec.RSAPublicKeySpec;

public class IngAtRsa {
    private int e;
    private BigInteger n;
    private BigInteger d;
    private boolean canEncrypt;
    private boolean canDecrypt;

    private IngAtRsa(final BigInteger n, final int E, final BigInteger d) {
        this.n = n;
        this.e = E;
        this.d = d;
        this.canEncrypt = this.n != null && e != 0;
        this.canDecrypt = canEncrypt && this.d != null;
    }

    public IngAtRsa(RSAPublicKeySpec rsaPublicKeySpec) {
        this(rsaPublicKeySpec.getModulus(), rsaPublicKeySpec.getPublicExponent().intValue(), null);
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
        return (BigDecimal.valueOf(n.bitLength()).add(BigDecimal.valueOf(7.0)))
                .divide(BigDecimal.valueOf(8.0), RoundingMode.FLOOR)
                .intValue();
    }

    private BigInteger doPublic(final BigInteger x) {
        if (this.canEncrypt) {
            return x.modPow(new BigInteger(this.e + ""), this.n);
        }

        return BigInteger.ZERO;
    }

    public String encrypt(final String text) {
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

    private BigInteger doPrivate(final BigInteger x) {
        if (this.canDecrypt) {
            return x.modPow(this.d, this.n);
        }

        return BigInteger.ZERO;
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
