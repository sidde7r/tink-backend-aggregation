package se.tink.backend.aggregation.agents.utils.authentication.encap;

import com.google.common.primitives.Bytes;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.KeyPair;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Security;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.assertj.core.api.Assertions;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.utils.crypto.AES;
import se.tink.backend.aggregation.agents.utils.crypto.EllipticCurve;
import se.tink.backend.aggregation.agents.utils.crypto.hash.Hash;

public class EmkStepsTest {
    // Test data represented in hex
    private static final String rand16BytesKeyInHex = "f492eb9fcc5154aa797b56ce2a2e33f6";
    private static final String rand16BytesIvInHex = "34cf5bfcaf853fa18814c92134c1dab5";
    private static final String ecKeyInHex =
            "01f821c9a438f311290d0ce3a9fbe38a9f4e3bb22154c4fc95144faeed00";
    private static final String clientGenPubKeyInHex =
            "0200f520b7852a436987743748038d2514a18626fbca0a96815df6f9c3f402";
    private static final String aesKeyInHex = "b599d8fbc05c55dc8f3449d23681c1bf";
    private static final String aesEcbOutputInHex =
            "199e187ed1db6bf0f635ae5036d69892a973bbf0bd7b9e23ddbb3f09906695fac1aa27ad6898013b1c82fd0b68dbf13f";
    private static final String hmacKeyInHex = "45a39b094355c7a9af962634fe45e3dc";
    private static final String expectedHmacOutputInHex =
            "badb16cfc4c34ddcab65595a60042b54a6838b47";
    private static final String expectedSha1CtrOutputInHex =
            "b599d8fbc05c55dc8f3449d23681c1bf45a39b094355c7a9af962634fe45e3dc";

    // Test data represented as array of bytes
    private static byte[] rand16BytesKey;
    private static byte[] rand16BytesIv;
    private static byte[] ecKey;
    private static byte[] clientGenPubKey;
    private static byte[] aesKey;
    private static byte[] aesEcbOutput;
    private static byte[] hmacKey;

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    @Before
    public void setUp() throws DecoderException {
        rand16BytesKey = Hex.decodeHex(rand16BytesKeyInHex.toCharArray());
        rand16BytesIv = Hex.decodeHex(rand16BytesIvInHex.toCharArray());
        ecKey = Hex.decodeHex(ecKeyInHex.toCharArray());
        clientGenPubKey = Hex.decodeHex(clientGenPubKeyInHex.toCharArray());
        aesKey = Hex.decodeHex(aesKeyInHex.toCharArray());
        aesEcbOutput = Hex.decodeHex(aesEcbOutputInHex.toCharArray());
        hmacKey = Hex.decodeHex(hmacKeyInHex.toCharArray());
    }

    @Test
    public void testEcdhDerive() {
        KeyPair keyPairA = EllipticCurve.generateKeyPair("sect233k1");
        KeyPair keyPairB = EllipticCurve.generateKeyPair("sect233k1");

        byte[] derivedKeyA =
                EllipticCurve.diffieHellmanDeriveKey(keyPairA.getPrivate(), keyPairB.getPublic());
        byte[] derivedKeyB =
                EllipticCurve.diffieHellmanDeriveKey(keyPairB.getPrivate(), keyPairA.getPublic());
        Assertions.assertThat(derivedKeyA).isEqualTo(derivedKeyB);
    }

    @Test
    public void testSha1CtrFunction() throws IOException {
        byte[] input = Bytes.concat(clientGenPubKey, ecKey);

        byte[] counter = new byte[4];
        counter[3] = 1;
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] sha1CtrRes = sha1CounterRecursion(input, counter, outputStream, 32);

        Assertions.assertThat(Hex.encodeHexString(sha1CtrRes))
                .isEqualTo(expectedSha1CtrOutputInHex);
    }

    private static byte[] sha1CounterRecursion(
            byte[] input, byte[] counter, ByteArrayOutputStream outputStream, int outputLen)
            throws IOException {

        byte[] sha1Res = sha1CounterFunction(input, counter);

        if (outputLen <= 20) {
            outputStream.write(sha1Res, 0, outputLen);
            return outputStream.toByteArray();
        } else {
            outputStream.write(sha1Res);
            counter[3]++;
            return sha1CounterRecursion(input, counter, outputStream, outputLen - 20);
        }
    }

    private static byte[] sha1CounterFunction(byte[] inputData, byte[] counter) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            md.update(inputData);
            md.update(counter);
            return md.digest();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    @Test
    public void testAesEcbEncryption() throws IOException {
        byte[] input = Bytes.concat(rand16BytesKey, rand16BytesIv);

        byte[] aesEcbRes = AES.encryptEcbPkcs5(aesKey, input);
        Assertions.assertThat(Hex.encodeHexString(aesEcbRes)).isEqualTo(aesEcbOutputInHex);
    }

    @Test
    public void testHmacFunction() throws IOException {
        byte[] input = Bytes.concat(aesEcbOutput, new byte[4]);
        Assertions.assertThat(Hash.hmacSha1AsHex(hmacKey, input))
                .isEqualTo(expectedHmacOutputInHex);
    }
}
