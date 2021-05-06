package se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.utils;

import java.security.PublicKey;
import java.security.Security;
import java.util.UUID;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.Assert;
import org.junit.Test;
import se.tink.backend.aggregation.agents.utils.crypto.EllipticCurve;
import se.tink.backend.aggregation.agents.utils.encoding.EncodingUtils;

public class ContentSignerTest {
    public static final String DEVICE_ID = UUID.randomUUID().toString();
    public static final String SESSION_ID = UUID.randomUUID().toString();

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    @Test
    public void shouldVerifiedSignatureFromTraffic() {
        // given
        String signatureInput =
                "/api/v2/auth/assert?aid=mobile_metro&did=4eed8a28-d956-45be-b8ab-9b77c37ee853&locale=en-US&sid=8af9fbbb-0211-412d-8bd3-9fc77ec32dee%%4.3.6;[1,2,3,6,7,8,10,11,12,14,28,19]%%{\"headers\":[{\"type\":\"uid\",\"uid\":\"115240534571\"}],\"data\":{\"action\":\"authentication\",\"assert\":\"authenticate\",\"assertion_id\":\"06/Ky/kvLOOHCSxW+i0pfzQq\",\"fch\":\"p0kixDEshjM0ygRyWxqaf2o3\",\"data\":{\"otp\":\"47335238\"},\"method\":\"otp\"}}";
        String signatureFromAnalyzer =
                "MEQCIBJQHgFztMmytGqSHIgf/ib8aE5jivF0H4n+DwseMCxXAiBfGdVUsqgdP8IUQ2N/Uz2+yQtd++OPBC2OnRCUjadTHA==";

        String ecPublicKey =
                "MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEHriS4+MdhRlqsVGPR3oOXlhnR5Rc6ZagFnzL9fZ96cI2vlHBlMIqRpuQB/N9vai9JhGJJz+X3t0pPjA6sH1SQA==";
        PublicKey publicKey =
                EllipticCurve.convertPEMtoPublicKey(EncodingUtils.decodeBase64String(ecPublicKey));

        byte[] bytes = EncodingUtils.decodeBase64String(signatureFromAnalyzer);

        // when
        boolean verified =
                EllipticCurve.verifySignSha256(publicKey, signatureInput.getBytes(), bytes);

        // then
        Assert.assertTrue(verified);
    }

    @Test
    public void shouldVerifiedSignatureWhichWasGeneratedByAgent() {
        // given
        String signatureInput =
                "/api/v2/auth/assert?aid=mobile_metro&did=4eed8a28-d956-45be-b8ab-9b77c37ee853&locale=en-US&sid=89b9d2cf-7470-4152-902c-c7ca21944361%%4.3.6;[1,2,3,6,7,8,10,11,12,14,28,19]%%{\"headers\":[{\"uid\":\"115240534571\",\"type\":\"uid\"}],\"data\":{\"assertion_id\":\"rQJXEbQ6VUhUQxZjA2vMtztn\",\"action\":\"authentication\",\"method\":\"otp\",\"fch\":\"rJOHI/6/EFqgfff0w3iUHlRu\",\"assert\":\"authenticate\",\"data\":{\"otp\":\"86090857\"}}}";
        String generateSignature =
                "MEQCIEg4i8MSzjVjfYtmUpPoN0kqEidAGEc+ihTLdaEZPG4JAiAsnMME9WNxjGS9shtMM2vVGk3y3R0xVaAPdxQbLkZklg==";

        String ecPublicKey =
                "MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAE3S2Mltq9giOFgVDSXqGjqRUu0VbLXewR2zhQvaAWXJ9y6jUshD4ELwlC96XNtw7cMTXTXDhXYcBS2KMtkhhLZQ==";
        PublicKey publicKey =
                EllipticCurve.convertPEMtoPublicKey(EncodingUtils.decodeBase64String(ecPublicKey));

        byte[] bytes = EncodingUtils.decodeBase64String(generateSignature);

        // when
        boolean verified =
                EllipticCurve.verifySignSha256(publicKey, signatureInput.getBytes(), bytes);

        // then
        Assert.assertTrue(verified);
    }
}
