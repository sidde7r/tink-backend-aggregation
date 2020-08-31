package se.tink.backend.aggregation.agents.nxgen.it.banks.isp.authenticator;

import org.assertj.core.api.Assertions;
import org.junit.Test;

public class TotpSeedXmlHelperTest {

    private static final String TEST_INPUT_STANDARD =
            "<response><status>success</status><aid>03409957-PIN1</aid><displayName>03409957-PIN1</displayName><logoUrl></logoUrl><algo><algoType>TOTP</algoType><cs>::DIGS=06::MPL_=5::ORG_=NDCE::TSTP=16::TVAR=35::TYPE=TOTP::UDK_=C8EFC25C440AB641132265749B539EBDBBDA76C1::UIDS=0001::UID_=03409957::USER=03409957::VER_=0.0.0::</cs></algo></response>";
    private static final String TEST_INPUT_DIFFERENT_TYPE =
            "<response><status>success</status><aid>03409957-PIN1</aid><displayName>03409957-PIN1</displayName><logoUrl></logoUrl><algo><algoType>TOTP</algoType><cs>::DIGS=06::MPL_=5::ORG_=NDCE::TSTP=16::TVAR=35::TYPE=TOTPx::UDK_=C8EFC25C440AB641132265749B539EBDBBDA76C1::UIDS=0001::UID_=03409957::USER=03409957::VER_=0.0.0::</cs></algo></response>";

    private static final String EXPECTED_TOTP_MASK = "C8EFC25C440AB641132265749B539EBDBBDA76C1";
    private static final int EXPECTED_TOTP_DIGITS = 6;

    @Test
    public void shouldExtractCorrectTotpDigits() {
        Assertions.assertThat(TotpSeedXmlHelper.getTotpDigits(TEST_INPUT_STANDARD))
                .isEqualTo(EXPECTED_TOTP_DIGITS);
    }

    @Test
    public void shouldExtractCorrectTotpMask() {
        Assertions.assertThat(TotpSeedXmlHelper.getTotpMask(TEST_INPUT_STANDARD))
                .isEqualTo(EXPECTED_TOTP_MASK);
    }

    @Test
    public void shouldThrowExceptionForUnknownType() {
        Assertions.assertThatThrownBy(
                        () -> TotpSeedXmlHelper.validateTotpType(TEST_INPUT_DIFFERENT_TYPE))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void shouldNotThrowExceptionForKnownType() {
        // implicit assert does not throw
        TotpSeedXmlHelper.validateTotpType(TEST_INPUT_STANDARD);
    }
}
