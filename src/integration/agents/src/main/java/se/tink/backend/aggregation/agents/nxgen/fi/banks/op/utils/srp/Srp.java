package se.tink.backend.aggregation.agents.nxgen.fi.banks.op.utils.srp;

import com.nimbusds.srp6.BigIntegerUtils;
import com.nimbusds.srp6.SRP6Routines;
import com.nimbusds.srp6.XRoutine;
import com.nimbusds.srp6.XRoutineWithUserIdentity;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Objects;
import se.tink.backend.aggregation.agents.utils.encoding.EncodingUtils;

public class Srp {
    // https://en.wikipedia.org/wiki/Secure_Remote_Password_protocol
    private static final String HASH_FUNCTION = "SHA-256";
    private final SRP6Routines srp6Routines;
    private final BigInteger N;
    private final BigInteger g;
    private final BigInteger a;
    private final BigInteger A;

    private Srp(BigInteger privateValue) {
        this.srp6Routines = new SRP6Routines();
        // Precomputed safe 2048-bit prime 'N', as decimal. Origin RFC 5054,
        // appendix A.
        this.N =
                new BigInteger(
                        "21766174458617435773191008891802753781907668374255538511144643224689886235383840957210909013086056401571399717235807266581649606472148410291413364152197364477180887395655483738115072677402235101762521901569820740293149529620419333266262073471054548368736039519702486226506248861060256971802984953561121442680157668000761429988222457090413873973970171927093992114751765168063614761119615476233422096442783117971236371647333871414335895773474667308967050807005509320424799678417036867928316761272274230314067548291133582479583061439577559347101961771406173684378522703483495337037655006751328447510550299250924469288819");

        // Generator 'g' parameter for `N_2048`.
        this.g = new BigInteger("2");

        // Private value.
        if (Objects.nonNull(privateValue)) {
            this.a = privateValue;
        } else {
            this.a = this.srp6Routines.generatePrivateValue(this.N, new SecureRandom());
        }

        // Public value.
        this.A = this.srp6Routines.computePublicClientValue(this.N, this.g, this.a);
    }

    static Srp withStaticPrivateValue(BigInteger privateValue) {
        return new Srp(privateValue);
    }

    private MessageDigest getDigestInstance() {
        try {
            return MessageDigest.getInstance(HASH_FUNCTION);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    private BigInteger computeX(byte[] salt, String userId, String password) {
        XRoutine xRoutine = new XRoutineWithUserIdentity();
        MessageDigest digest = this.getDigestInstance();
        return xRoutine.computeX(
                digest,
                salt,
                userId.getBytes(StandardCharsets.UTF_8),
                password.getBytes(StandardCharsets.UTF_8));
    }

    private BigInteger computeU(BigInteger B) {
        MessageDigest digest = this.getDigestInstance();
        return this.srp6Routines.computeU(digest, this.N, this.A, B);
    }

    private BigInteger computeK() {
        MessageDigest digest = this.getDigestInstance();
        return this.srp6Routines.computeK(digest, this.N, this.g);
    }

    private BigInteger computeSessionKey(BigInteger x, BigInteger u, BigInteger B) {
        BigInteger k = this.computeK();
        return this.srp6Routines.computeSessionKey(this.N, this.g, k, x, u, this.a, B);
    }

    private BigInteger computeClientEvidenceMessage(
            BigInteger sessionKey, byte[] salt, String userId, BigInteger B) {
        MessageDigest digest = this.getDigestInstance();

        digest.update(BigIntegerUtils.bigIntegerToBytes(this.N));
        byte[] hN = digest.digest();

        digest.update(BigIntegerUtils.bigIntegerToBytes(this.g));
        byte[] hg = digest.digest();

        byte[] hNhg = xor(hN, hg);

        digest.update(userId.getBytes());
        byte[] hu = digest.digest();

        digest.update(BigIntegerUtils.bigIntegerToBytes(sessionKey));
        byte[] hS = digest.digest();

        digest.update(hNhg);
        digest.update(hu);
        digest.update(salt);
        digest.update(BigIntegerUtils.bigIntegerToBytes(this.A));
        digest.update(BigIntegerUtils.bigIntegerToBytes(B));
        digest.update(hS);

        return new BigInteger(1, digest.digest());
    }

    private static byte[] xor(byte[] b1, byte[] b2) {
        byte[] result = new byte[b1.length];
        for (int i = 0; i < b1.length; i++) {
            result[i] = (byte) (b1[i] ^ b2[i]);
        }
        return result;
    }

    ClientEvidenceMessageResponse calculateClientEvidenceMessage(
            String serverPublicValueAsHex, String saltAsHex, String userId, String password) {
        BigInteger B = new BigInteger(EncodingUtils.decodeHexString(serverPublicValueAsHex));
        byte[] salt = EncodingUtils.decodeHexString(saltAsHex);

        BigInteger x = this.computeX(salt, userId, password);
        BigInteger u = this.computeU(B);

        BigInteger sessionKey = this.computeSessionKey(x, u, B);

        BigInteger evidenceMessage = this.computeClientEvidenceMessage(sessionKey, salt, userId, B);

        return new ClientEvidenceMessageResponse(this.A, evidenceMessage, sessionKey);
    }
}
