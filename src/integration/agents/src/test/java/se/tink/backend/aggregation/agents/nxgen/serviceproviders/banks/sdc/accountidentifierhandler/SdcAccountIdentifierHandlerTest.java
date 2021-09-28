package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.accountidentifierhandler;

import static org.assertj.core.api.Assertions.assertThat;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JUnitParamsRunner.class)
public class SdcAccountIdentifierHandlerTest {

    private final SdcAccountIdentifierHandler accountIdentifierHandler =
            DefaultSdcAccountIdentifierHandler.DK_ACCOUNT_IDENTIFIER_HANDLER;

    @Test
    @Parameters({
        "0, true",
        "1342, true",
        "345685769857456, true",
        "-245, false",
        "53.2367306, false",
        "367T306, false",
        "+1342, false",
        "-1342, false",
    })
    public void containsDigitsOnly(final String accountNo, final boolean expectedResult) {
        // given

        // when
        boolean result = accountIdentifierHandler.containsDigitsOnly(accountNo);

        // then
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    @Parameters({"4, ABCD", "2, ABCD", "6, 00ABCD"})
    public void containsDigitsOnly(final int noOfChars, final String expectedResult) {
        // given
        String sampleAccount = "ABCD";

        // when
        String result = accountIdentifierHandler.prefixWithZeros(sampleAccount, noOfChars);

        // then
        assertThat(result).isEqualTo(expectedResult);
    }
}
