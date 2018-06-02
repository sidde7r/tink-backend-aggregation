package se.tink.backend.aggregation.agents.nxgen.be.banks.ing;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import se.tink.backend.aggregation.agents.utils.encoding.EncodingUtils;

public class IngCryptoTest {

    private static final byte[] pinCode = "7894".getBytes();
    private static final byte[] secret0 = EncodingUtils.decodeHexString("289c3d7fa71c6e0a5b5daa88cfebcecc");
    private static final byte[] secret1 = EncodingUtils.decodeHexString("27d80e101fb8278f6c2150ab03f2b1ae");
    private static final byte[] key = EncodingUtils.decodeHexString("e36bcfc576c3ba6fd4b6c34e12924b41");

    @Test
    public void testIngDeriveKey() {
        String expectedKey = EncodingUtils.encodeHexAsString(key);
        String derivedKey = EncodingUtils.encodeHexAsString(IngCryptoUtils.deriveOtpKey(pinCode, secret0, secret1));
        Assertions.assertThat(expectedKey).isEqualTo(derivedKey);
    }

    @Test
    public void testIngOtp() {
        int[] exptectedOtps = new int[] {85636, 135807};

        for (int i=0; i<exptectedOtps.length; i++) {
            int calculatedOtp = IngCryptoUtils.calculateOtpForAuthentication(key, i+1); // start at 1

            Assertions.assertThat(exptectedOtps[i]).isEqualTo(calculatedOtp);
        }
    }

    @Test
    public void testDecryptCredentials() {
        String encryptedCredentialsResponse = "wApZsYOw4MJsnbfsS6Bn5KXN5X1zh9DP7SOEOXvo258d7I46DMYcxaKpVzW2pl8Yc/NLZneO6D0FalXes8rzVZCL1elBhMlLRsEhzoGa+dk=";
        byte[] sessionKey = EncodingUtils.decodeHexString("b72988842687530e2b95961d6cb22446"); // randomly generated and sent to server
        byte[] sessionKeyAuth = EncodingUtils.decodeHexString("47b744c79c6c5939f1229093c62b8ed7"); // randomly generated and sent to server

        byte[] decryptedMessage = IngCryptoUtils.decryptAppCredentials(
                encryptedCredentialsResponse,
                sessionKey,
                sessionKeyAuth);

        String expectedResultInHex = "0301100317e93b871d395eede0dd657d4f13eb6a25cf98e25f038f14fbd6d697759d939ce9469125d6f0c6f9e62e8638";
        Assertions.assertThat(expectedResultInHex).isEqualTo(EncodingUtils.encodeHexAsString(decryptedMessage));
    }

    @Test
    public void testOtpForTransferToSavedBenificiary() {
        byte[] otpKey = EncodingUtils.decodeHexString("b748790ff150efd57cf86fdc887261a4");

        int calculatedOtp1 = IngCryptoUtils.calcOtpForSigningTransfer(otpKey, 10, "1050", "797962997");
        int calculatedOtp2 = IngCryptoUtils.calcOtpForSigningTransfer(otpKey, 69, "150", "1797962");

        Assertions.assertThat(calculatedOtp1).isEqualTo(669488);
        Assertions.assertThat(calculatedOtp2).isEqualTo(4537292);
    }

    @Test
    public void testOtpForTransferToOtherAccount() {
        byte[] otpKey = EncodingUtils.decodeHexString("b748790ff150efd57cf86fdc887261a4");

        int calculatedOtp1 = IngCryptoUtils.calcOtpForSigningTransfer(otpKey, 80, "200", "100880177");
        int calculatedOtp2 = IngCryptoUtils.calcOtpForSigningTransfer(otpKey, 81, "500", "7100880");

        Assertions.assertThat(calculatedOtp1).isEqualTo(5274730);
        Assertions.assertThat(calculatedOtp2).isEqualTo(5330531);
    }

    @Test
    public void testDataToSignFormatting() {
        byte[] dataToSign1 = IngCryptoUtils.getFormattedDataToSign("300", "9796299");
        byte[] dataToSign2 = IngCryptoUtils.getFormattedDataToSign("1030", "9796299");
        byte[] dataToSign3 = IngCryptoUtils.getFormattedDataToSign("1050", "962997");
        byte[] dataToSign4 = IngCryptoUtils.getFormattedDataToSign("1050", "97962997");
        byte[] dataToSign5 = IngCryptoUtils.getFormattedDataToSign("1050", "797962997");
        byte[] dataToSign6 = IngCryptoUtils.getFormattedDataToSign("150000", "179796299");
        byte[] dataToSign7 = IngCryptoUtils.getFormattedDataToSign("25000", "179796299");

        Assertions.assertThat(EncodingUtils.encodeHexAsString(dataToSign1)).isEqualTo("300f9796299f8000");
        Assertions.assertThat(EncodingUtils.encodeHexAsString(dataToSign2)).isEqualTo("1030f97962998000");
        Assertions.assertThat(EncodingUtils.encodeHexAsString(dataToSign3)).isEqualTo("1050f962997f8000");
        Assertions.assertThat(EncodingUtils.encodeHexAsString(dataToSign4)).isEqualTo("1050f97962997f80");
        Assertions.assertThat(EncodingUtils.encodeHexAsString(dataToSign5)).isEqualTo("1050f79796299780");
        Assertions.assertThat(EncodingUtils.encodeHexAsString(dataToSign6)).isEqualTo("150000f1797962998000000000000000");
        Assertions.assertThat(EncodingUtils.encodeHexAsString(dataToSign7)).isEqualTo("25000f179796299f8000000000000000");
    }
}
