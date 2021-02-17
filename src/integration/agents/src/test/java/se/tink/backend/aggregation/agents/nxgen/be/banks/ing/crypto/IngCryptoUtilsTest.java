package se.tink.backend.aggregation.agents.nxgen.be.banks.ing.crypto;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import javax.crypto.spec.SecretKeySpec;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.encoders.Hex;
import org.junit.Test;
import se.tink.backend.aggregation.agents.utils.crypto.EllipticCurve;

public class IngCryptoUtilsTest {

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    private static final String MY_PRIVATE_KEY =
            "MIGHAgEAMBMGByqGSM49AgEGCCqGSM49AwEHBG0wawIBAQQg3GiofKICcwb2JFlV"
                    + "LsPt67YxHVGyYfY7clk1Py9UTdqhRANCAATJ9YsSyPlylY1lBZpwitDybL+XhdGC"
                    + "MfdIFClEzLOWyl2l7NiGPZs8HuZ/Hy+xHSbHOJcmPBl7+o7Q0r3fBqmZ";

    private static final String THEIR_PUBLIC_KEY =
            "MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEC0tyXIuOUZUeHVQS1nr59ushAdMU"
                    + "WG/fdmuQ/gPLP9gN5KrLDpvb5WfJPqHMQqhREpDsTKwYut9lzNZqnZaPyQ==";

    private IngCryptoUtils ingCryptoUtils = new IngCryptoUtils();

    @Test
    public void shouldDeriveSigningAndEncryptionKeys() throws Exception {
        byte[] privateKeyBytes = Base64.getDecoder().decode(MY_PRIVATE_KEY);
        byte[] publicKeyBytes = Base64.getDecoder().decode(THEIR_PUBLIC_KEY);

        byte[] salt =
                Hex.decode("64d395c538922bfe0740ceeae92bf35f97b95e68c94aed0c5061f7edee36b162");

        byte[] clientId =
                Hex.decode(
                        "37303637313165642d383131312d346436352d613033352d373434313038336532303739");

        PKCS8EncodedKeySpec encodedKeySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("EC");
        PrivateKey privateKey = keyFactory.generatePrivate(encodedKeySpec);

        X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(publicKeyBytes);
        PublicKey publicKey = keyFactory.generatePublic(x509EncodedKeySpec);

        DerivedKeyOutput derivedKeyOutput =
                ingCryptoUtils.deriveKeys(privateKey, publicKey, salt, clientId);

        byte[] expectedEncryptionKey =
                Hex.decode("052c17a3e22473467fefd3b4041e1a8cd4e86cf5bd6b733a4d5edf550971cec7");
        byte[] expectedSigningKey =
                Hex.decode(
                        "a9051f4ab4923bf1d4b98b4aa73d7d56a911aa5fbd14980fb886e129e8ec75695c2fc9bd711209dbb7393dc3cf8e858f5cf83512330fa456be7c2345e9ed62b0");

        assertThat(derivedKeyOutput).isNotNull();
        assertThat(derivedKeyOutput.getEncryptionKey().getEncoded())
                .isEqualTo(expectedEncryptionKey);
        assertThat(derivedKeyOutput.getSigningKey().getEncoded()).isEqualTo(expectedSigningKey);
    }

    @Test
    public void shouldVerifyEvidenceSignature() {
        String evidence = "f8eb311afabdce91481c874ac09caec04236c20819fa89a2a03f428495146826";
        String signature =
                "MEUCIQCyp3SepTpPZHuCrbyS2XJnJELPPUmy6XojwsebDLMIKQIgbuM0/5QUKpThF1VN047F0I7lMj5mNuOuBUIEp4lRDe4=";
        String pinningKey =
                "MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEMxrw6XPifQc3dD+pVs4JIjbSp/pHM4joMklXJdfxXj5Si8NjS930dpNlEjqk9YYb/KzdpNL/Ul7wtPje8XAOXA==";

        PublicKey publicKey =
                EllipticCurve.convertPEMtoPublicKey(Base64.getDecoder().decode(pinningKey));

        boolean result = ingCryptoUtils.verifyEvidence(Hex.decode(evidence), signature, publicKey);

        assertThat(result).isTrue();
    }

    @Test
    public void shouldDecodeExtra() throws Exception {
        String extraFile =
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/be/banks/ing/resources/extra_response.txt";

        String extra = new String(Files.readAllBytes(Paths.get(extraFile)));
        extra = extra.replace("\n", "");

        String encodedEncryptionKey =
                "90f1ae6d9469be3951614678dea36644b25adac7a5deb1c7167412f379a89c57";
        SecretKeySpec encryptionKey = new SecretKeySpec(Hex.decode(encodedEncryptionKey), "AES");

        String result = ingCryptoUtils.decryptExtra(extra, encryptionKey);
        assertThat(result).startsWith("{").endsWith("}");
    }

    @Test
    public void shouldGenerateVerifier() {
        String mobileAppId = "58f2c2b2-3817-44c4-babd-a8e3595f8337";
        byte[] randomPassword =
                Hex.decode(
                        "83c00aedc220d87b2accf0599d7fac6efa39edcad433a3899fc213f293c94cfd"
                                + "457f2a67702b328aaced0f3ed7b243bca0e702e435b9a12034175ff95ba0670c"
                                + "d7a181ab799351b8586be4641c03b5654d7d4e488dda4d9695adec66fb544253"
                                + "06e56dc4adc13ca906626c83a1e3f903fb5c51b5562a4ed5365f74f660bff24d"
                                + "c1e9420128f0d2f21b8ff375afdb8a9f90575bf8654925e39317954a231a3988");

        byte[] salt = Hex.decode("C4CDB68313AA0C7802DD045AB71A4A94");

        String verifier = ingCryptoUtils.generateSRP6Verifier(salt, mobileAppId, randomPassword);

        assertThat(verifier)
                .isEqualTo(
                        "42E50CC99E9F01F48B31DFE6707532923CDA2D62288FFC8F89FA37A14A8A1FF1"
                                + "41D37A18EF0B8E4D61FC459B55F89AC214B3559F986FAC8BA9002D974FC71327"
                                + "0EAACA1837BF83A3DC42285A1E2451C822F3E99A03A1882BEFF6D72C4BAF4D35"
                                + "4E62CC8107A433629E3D89624CA4EC0E1B6192FE4F9A1EE2DFDC8CD2D35BCCE0");
    }
}
