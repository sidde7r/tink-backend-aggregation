package se.tink.backend.aggregation.agents.utils.crypto.hash;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import org.junit.Assert;
import org.junit.Test;
import se.tink.backend.aggregation.agents.utils.encoding.EncodingUtils;

public class HashTest {
    private static final String EXPECTED = "7f9a6871b86f40c330132c4fc42cda59";

    @Test
    public void testMd5() {
        byte[] hash = Hash.md5("tinkerbell");

        Assert.assertArrayEquals(EncodingUtils.decodeHexString(EXPECTED), hash);
    }

    @Test
    public void testSha256() {
        String ser =
                "{\"paymentInformationId\":\"IrMW1PoRgoqspA6yzWsvoaDhnH6jNjVTZL7\",\"creationDateTime\":\"2021-08-03T13:28:03.267+02:00\",\"requestedExecutionDate\":\"2021-08-04T00:01:00.000+02:00\",\"numberOfTransactions\":1,\"paymentTypeInformation\":{\"serviceLevel\":\"SEPA\",\"categoryPurpose\":\"CASH\"},\"statusReasonInformation\":null,\"beneficiary\":{\"creditor\":{\"name\":\"Valérie Augendre\"},\"creditorAccount\":{\"iban\":\"FR1420041010050500013M02606\"}},\"creditTransferTransaction\":[{\"paymentId\":{\"instructionId\":\"SV8Njp91iaYmt0Ymc3tfbUcEXiivbg7BZmK\",\"endToEndId\":\"Tizssmtznp33cEeVN1nwE2CxSBUJM2ofQH1\"},\"remittanceInformation\":{\"unstructured\":[\"Test\"]},\"instructedAmount\":{\"amount\":\"1.0\",\"currency\":\"EUR\"}}],\"chargeBearer\":\"SLEV\",\"supplementaryData\":{\"acceptedAuthenticationApproach\":[\"REDIRECT\"],\"successfulReportUrl\":\"https://api.tink.com/api/v1/credentials/third-party/callback?state=15f7d19b-cb14-4bdb-b4af-eeb7ebe0feed\",\"unsuccessfulReportUrl\":\"https://api.tink.com/api/v1/credentials/third-party/callback?state=15f7d19b-cb14-4bdb-b4af-eeb7ebe0feed\"},\"initiatingParty\":{\"name\":\"TINK\"},\"paymentInformationStatus\":null}";
        Assert.assertEquals(
                "[-42, 44, -35, 93, 74, 109, 123, -90, -25, 30, 40, 123, -84, -75, -124, 102, -102, 24, 29, 109, -23, -67, -123, 55, 62, -117, 38, -53, -33, -37, 32, -15]",
                Arrays.toString(Hash.sha256(ser.getBytes(StandardCharsets.UTF_8))));
    }
}
