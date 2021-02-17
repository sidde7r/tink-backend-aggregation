package se.tink.backend.aggregation.agents.nxgen.be.banks.axa.utils;

import java.security.KeyPair;
import java.security.PublicKey;
import java.security.Security;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import org.assertj.core.api.Assertions;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.Assert;
import org.junit.Test;
import se.tink.backend.aggregation.agents.utils.crypto.EllipticCurve;
import se.tink.backend.aggregation.agents.utils.encoding.EncodingUtils;

public class AxaCryptoUtilTest {

    private static final String EC_PUBLIC_KEY_BASE64 =
            "MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEJgA//oZrkFVUZFbL5iG1Fbysyn1d+8RTtR6/u0qAOH38Q5mN3mgG3rkk/cy8wp+rD4rOb8Oco+w/nRh9/GnHnQ==";

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    @Test
    public void testGeneratingRSAKeyPair() {
        KeyPair keyPair = AxaCryptoUtil.generateRSAKeyPair();

        Assertions.assertThat(keyPair.getPublic()).isInstanceOf(RSAPublicKey.class);
        Assertions.assertThat(keyPair.getPrivate()).isInstanceOf(RSAPrivateKey.class);
    }

    @Test
    public void testGeneratingRequestSignatureECKeyPair() {
        KeyPair keyPair = AxaCryptoUtil.generateRequestSignatureECKeyPair();

        Assertions.assertThat(keyPair.getPublic()).isInstanceOf(ECPublicKey.class);
        Assertions.assertThat(keyPair.getPrivate()).isInstanceOf(ECPrivateKey.class);
    }

    @Test
    public void testGeneratingChallengeSignECKeyPair() {
        KeyPair keyPair = AxaCryptoUtil.generateChallengeSignECKeyPair();

        Assertions.assertThat(keyPair.getPublic()).isInstanceOf(ECPublicKey.class);
        Assertions.assertThat(keyPair.getPrivate()).isInstanceOf(ECPrivateKey.class);
    }

    @Test
    public void testRawEcPublicKeyRepresentation() {
        KeyPair keyPair = AxaCryptoUtil.generateChallengeSignECKeyPair();
        String rawEcPublicKey = AxaCryptoUtil.getRawEcPublicKey(keyPair);
        byte[] bytes = EncodingUtils.decodeBase64String(rawEcPublicKey);

        Assertions.assertThat(rawEcPublicKey).hasSize(88);
        Assertions.assertThat(bytes).hasSize(65);
    }

    @Test
    public void testCreatingSignedFch() {
        KeyPair keyPair = AxaCryptoUtil.generateChallengeSignECKeyPair();
        String signedFch =
                AxaCryptoUtil.createSignedFch(
                        keyPair, "HsxXRsZhLhUmp8SGf83WoUSI", "8TAYd3c6K6N/NF1nh995wXdF");
        byte[] signedFchBytes = EncodingUtils.decodeBase64String(signedFch);
        Assertions.assertThat(signedFch).hasSize(88);
        Assertions.assertThat(signedFchBytes).hasSize(64);
    }

    @Test
    public void testHeaderSignatureAgainstPublicKey() {
        String signatureInput =
                "/api/v2/auth/assert?aid=mobile&did=39e1e83f-01a4-45bd-b64e-7f65d286de91&sid=630a2638-b406-4898-8638-67d9558e62ec%%3.6.13;[1,2,3,6,7,8,10,11,12,14,19]%%{\"headers\":[{\"type\":\"uid\",\"uid\":\"8a407e28-f2b8-4722-8114-44dbe0bafa42\"}],\"data\":{\"action\":\"authentication\",\"assert\":\"authenticate\",\"assertion_id\":\"+EKH8VXzD/b3pyrWwI4MNhm6\",\"fch\":\"r2PL3BoZKCsqknBX08cFwAwlHRS1scLda+eYV9RAHNNGgx5q27zLKBadiOHgDnf3SwN95/8NOYyi4UoOPzXYMg==\",\"data\":{},\"method\":\"pin\"}}";
        String signatureFromAnalyzer =
                "MEUCIQC5uSm3xlHVuldlKUDbMKPqtLSGp3KTO6DD+6Mj3Ubd3AIgJ5Ai3vA6ArjoKfTYBS805xDTI3FNswRpqN5Q7CCcNLU=";
        PublicKey publicKey =
                EllipticCurve.convertPEMtoPublicKey(
                        EncodingUtils.decodeBase64String(EC_PUBLIC_KEY_BASE64));
        byte[] bytes = EncodingUtils.decodeBase64String(signatureFromAnalyzer);

        Assert.assertTrue(
                EllipticCurve.verifySignSha256(publicKey, signatureInput.getBytes(), bytes));
    }

    @Test
    public void testCreatingHeaderSignature() {
        String httpRequestPath =
                "/api/v2/auth/assert?aid=mobile&did=39e1e83f-01a4-45bd-b64e-7f65d286de91&sid=630a2638-b406-4898-8638-67d9558e62ec";
        String clientVersion = "3.6.13;[1,2,3,6,7,8,10,11,12,14,19]";
        String requestBody =
                "{\"headers\":[{\"type\":\"uid\",\"uid\":\"8a407e28-f2b8-4722-8114-44dbe0bafa42\"}],\"data\":{\"action\":\"authentication\",\"assert\":\"authenticate\",\"assertion_id\":\"+EKH8VXzD/b3pyrWwI4MNhm6\",\"fch\":\"r2PL3BoZKCsqknBX08cFwAwlHRS1scLda+eYV9RAHNNGgx5q27zLKBadiOHgDnf3SwN95/8NOYyi4UoOPzXYMg==\",\"data\":{},\"method\":\"pin\"}}";
        KeyPair keyPair = AxaCryptoUtil.generateRequestSignatureECKeyPair();
        String headerSignature =
                AxaCryptoUtil.createHeaderSignature(
                        keyPair, httpRequestPath, clientVersion, requestBody);

        Assertions.assertThat(headerSignature).hasSize(96);
    }

    @Test
    public void testSigningPayloadInRequest() {
        String signatureInput =
                "{\"params\":{\"title\":\"Confirm\",\"text\":\"Register fingerprint\",\"continue_button_text\":\"Yes\",\"cancel_button_text\":\"No\",\"parameters\":[]},\"user_input\":\"No\"}";
        String signature =
                "MEUCIQCezre3t6FGqPaVJSH8OWluHQxJ45AHyq/kjgRvddWzeQIgVzPWL8Ko2of1A7v7rvDdmjXtj28Jxh9U1eOARasaDNo=";
        PublicKey publicKey =
                EllipticCurve.convertPEMtoPublicKey(
                        EncodingUtils.decodeBase64String(EC_PUBLIC_KEY_BASE64));
        byte[] bytes = EncodingUtils.decodeBase64String(signature);

        Assert.assertTrue(
                EllipticCurve.verifySignSha256(publicKey, signatureInput.getBytes(), bytes));
    }

    @Test
    public void testGeneratingECKeyPairFromEncodedString() {
        KeyPair keyPair = AxaCryptoUtil.generateChallengeSignECKeyPair();
        String encodedPublicKey =
                EncodingUtils.encodeAsBase64String(keyPair.getPublic().getEncoded());
        String encodedPrivateKey =
                EncodingUtils.encodeAsBase64String(keyPair.getPrivate().getEncoded());

        KeyPair recreatedKeyPair =
                AxaCryptoUtil.generateKeyPairFromBase64(encodedPublicKey, encodedPrivateKey);
        String recreatedEncodedPublicKey =
                EncodingUtils.encodeAsBase64String(recreatedKeyPair.getPublic().getEncoded());
        String recreatedEncodedPrivateKey =
                EncodingUtils.encodeAsBase64String(recreatedKeyPair.getPrivate().getEncoded());

        Assertions.assertThat(recreatedEncodedPublicKey).isEqualTo(encodedPublicKey);
        Assertions.assertThat(recreatedEncodedPrivateKey).isEqualTo(encodedPrivateKey);
    }
}
