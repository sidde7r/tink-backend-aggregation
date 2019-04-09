package se.tink.backend.aggregation.agents.utils.authentication.vasco.digipass.utils;

import java.util.Collections;
import org.junit.Assert;
import org.junit.Test;
import se.tink.backend.aggregation.agents.utils.authentication.vasco.digipass.StaticVector;
import se.tink.backend.aggregation.agents.utils.encoding.EncodingUtils;

public class OtpUtilsTest {
    private static final String FINGERPRINT =
            "CF190D3E7E8D6C460DADBB9D9C7E0E63FE5AD07E5E1A0B32320EBAF4978503E3";
    private static final String XFAD =
            "38080150010346445102105E4532DFD9570783834970C140C764E303010104010605010606010107010108010509010F0A01010B01000C01000E01010F01011001011135120100130101140101150C4158414D6F624C6F676F6E201601011704008298D11801011901102101102901102A01002B01002C01021147120100130101140102150C4158414D6F625369676E20201601011704008298D11801041901011A01011B01011C01012101102201102301102401102901102A01002B01002C0102115F120100130101140103150C4158415369676E20202020201601011704008298D11801081901011A01001B01001C01001D01001E01001F01002001002101102201102301102401102501102601102701102801102901102A01002B01002C0102112F120100130101140104150C415841526573706F6E73652016010117040080D8011801002901102A01002B01002C010251887297C0778DEEEFC68914";
    private static final long EPOCH_TIME = 1548682192;
    private static final String PRE_TIME_XOR_DATA = "18ff65deb72a823b";
    private static final String EXPECTED_TIME_XORED_DATA = "18ff65deb2ee7206";
    private static final String OTP_KEY = "4a34522f1f6b8fe526a4de6a60cac1d1";
    private static final String DIVERSIFIER = "f5de9c78";
    private static final String CHALLENGE_0 = "2969693806887446";
    private static final String CHALLENGE_1 = "0609812170961669";
    private static final String EXPECTED_CHALLENGE_0_MASHUP = "5b630d3592c9a165";
    private static final String EXPECTED_CHALLENGE_0_RESPONSE = "2997460261106911";
    private static final String EXPECTED_CHALLENGE_1_RESPONSE = "6247441762747670";
    private static final String EXPECTED_DERIVATION_CODE = "03624476206247441762747670";

    @Test
    public void testTimeXor() {
        byte[] data = EncodingUtils.decodeHexString(PRE_TIME_XOR_DATA);
        byte[] timeData = OtpUtils.xorWithTime(data, EPOCH_TIME);
        String timeDataHex = EncodingUtils.encodeHexAsString(timeData);

        Assert.assertEquals(
                "(epoch>>4) ^ data did not match.", EXPECTED_TIME_XORED_DATA, timeDataHex);
    }

    @Test
    public void testMashupChallenge() {
        byte[] key = EncodingUtils.decodeHexString(OTP_KEY);
        byte[] diversifier = EncodingUtils.decodeHexString(DIVERSIFIER);

        byte[] mashup =
                OtpUtils.mashupChallenge(
                        key,
                        diversifier,
                        0,
                        EPOCH_TIME,
                        Collections.singletonList(EncodingUtils.decodeHexString(CHALLENGE_0)));
        String mashupHex = EncodingUtils.encodeHexAsString(mashup);

        Assert.assertEquals("mashupChallenge mismatch.", EXPECTED_CHALLENGE_0_MASHUP, mashupHex);
    }

    @Test
    public void testCalculateOtp() {
        byte[] key = EncodingUtils.decodeHexString(OTP_KEY);
        StaticVector staticVector = StaticVector.createFromXfad(XFAD);

        String otpResponse =
                OtpUtils.calculateOtp(
                        FINGERPRINT,
                        staticVector,
                        key,
                        0,
                        EPOCH_TIME,
                        Collections.singletonList(EncodingUtils.decodeHexString(CHALLENGE_0)));

        Assert.assertEquals(
                "Challenge response was not correct.", EXPECTED_CHALLENGE_0_RESPONSE, otpResponse);
    }

    @Test
    public void testCalculateDerivationCode() {
        StaticVector staticVector = StaticVector.createFromXfad(XFAD);
        String derivationCode =
                OtpUtils.calculateDerivationCode(
                        FINGERPRINT, staticVector, EXPECTED_CHALLENGE_1_RESPONSE);

        Assert.assertEquals(
                "Derivation code was not correct.", EXPECTED_DERIVATION_CODE, derivationCode);
    }
}
