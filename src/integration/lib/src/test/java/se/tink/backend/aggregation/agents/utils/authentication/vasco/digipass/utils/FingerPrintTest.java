package se.tink.backend.aggregation.agents.utils.authentication.vasco.digipass.utils;

import org.junit.Assert;
import org.junit.Test;
import se.tink.backend.aggregation.agents.utils.authentication.vasco.digipass.StaticVector;
import se.tink.backend.aggregation.agents.utils.encoding.EncodingUtils;

public class FingerPrintTest {
    private static final String XFAD =
            "38080150010346445102105E4532DFD9570783834970C140C764E303010104010605010606010107010108010509010F0A01010B01000C01000E01010F01011001011135120100130101140101150C4158414D6F624C6F676F6E201601011704008298D11801011901102101102901102A01002B01002C01021147120100130101140102150C4158414D6F625369676E20201601011704008298D11801041901011A01011B01011C01012101102201102301102401102901102A01002B01002C0102115F120100130101140103150C4158415369676E20202020201601011704008298D11801081901011A01001B01001C01001D01001E01001F01002001002101102201102301102401102501102601102701102801102901102A01002B01002C0102112F120100130101140104150C415841526573706F6E73652016010117040080D8011801002901102A01002B01002C010251887297C0778DEEEFC68914";
    private static final String FINGERPRINT =
            "CF190D3E7E8D6C460DADBB9D9C7E0E63FE5AD07E5E1A0B32320EBAF4978503E3";
    private static final String EXPECTED_DIVERSIFIER = "f5de9c78";

    @Test
    public void testDiversifier() {
        StaticVector staticVector = StaticVector.createFromXfad(XFAD);
        byte[] diversifier = FingerPrintUtils.getFingerPrintDiversifier(FINGERPRINT, staticVector);
        String diversifierHex = EncodingUtils.encodeHexAsString(diversifier);

        Assert.assertEquals(
                "Fingerprint diversifier mismatch.", EXPECTED_DIVERSIFIER, diversifierHex);
    }
}
