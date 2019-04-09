package se.tink.backend.aggregation.agents.utils.authentication.vasco.digipass.utils;

import org.junit.Assert;
import org.junit.Test;
import se.tink.backend.aggregation.agents.utils.encoding.EncodingUtils;

public class XfadTest {
    private static final String XFAD =
            "38080150010346445102105E4532DFD9570783834970C140C764E303010104010605010606010107010108010509010F0A01010B01000C01000E01010F01011001011135120100130101140101150C4158414D6F624C6F676F6E201601011704008298D11801011901102101102901102A01002B01002C01021147120100130101140102150C4158414D6F625369676E20201601011704008298D11801041901011A01011B01011C01012101102201102301102401102901102A01002B01002C0102115F120100130101140103150C4158415369676E20202020201601011704008298D11801081901011A01001B01001C01001D01001E01001F01002001002101102201102301102401102501102601102701102801102901102A01002B01002C0102112F120100130101140104150C415841526573706F6E73652016010117040080D8011801002901102A01002B01002C010251887297C0778DEEEFC68914";
    private static final String EXPECTED_REMAINDER = "51887297c0778deeefc68914";
    private static final String EXPECTED_OTP_SEED_IV = "6670693239";
    private static final String EXPECTED_OTP_SEED_DATA = "7c0778deeefc6891";

    @Test
    public void testXfadRemainder() {
        byte[] xfad = EncodingUtils.decodeHexString(XFAD);

        String remainder = XfadUtils.getRemainderHexEncoded(xfad);

        Assert.assertEquals("Wrong xfad->remainder", EXPECTED_REMAINDER, remainder);
    }

    @Test
    public void testOtpSeedIv() {
        byte[] xfad = EncodingUtils.decodeHexString(XFAD);

        byte[] otpSeedIv = XfadUtils.getOtpSeedIv(xfad);
        String otpSeedIvHex = EncodingUtils.encodeHexAsString(otpSeedIv);

        Assert.assertEquals("Wrong Xfad->otpSeedIv", EXPECTED_OTP_SEED_IV, otpSeedIvHex);
    }

    @Test
    public void testOtpSeedData() {
        byte[] xfad = EncodingUtils.decodeHexString(XFAD);

        byte[] otpSeedData = XfadUtils.getOtpSeedData(xfad);
        String otpSeedDataHex = EncodingUtils.encodeHexAsString(otpSeedData);

        Assert.assertEquals("Wrong Xfad->otpSeedData", EXPECTED_OTP_SEED_DATA, otpSeedDataHex);
    }
}
