package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import io.vavr.control.Option;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JUnitParamsRunner.class)
public class BbvaUtilsTest {

    private static final String NEXT_KEY = "70";
    private static final String PAGINATION_KEY = "&paginationKey=";
    private static final String URL_TO_PARSE =
            "/cardTransactions/V01/?contractId=CONTRACTID&cardTransactionType=C"
                    + PAGINATION_KEY
                    + NEXT_KEY
                    + "&pageSize=50";
    private static final String URL_TO_PARSE_NO_KEY =
            "/cardTransactions/V01/?contractId=CONTRACTID&cardTransactionType=C" + "&pageSize=50";

    @Test
    public void splitUtlGetKey_properNextKey() {
        Option<String> nextKeyString = BbvaUtils.splitGetPaginationKey(URL_TO_PARSE);
        assertTrue(nextKeyString.isDefined());
        assertEquals(NEXT_KEY, nextKeyString.get());
    }

    @Test
    @Parameters({"AAA1234567", "AAAA123456", "aaaa123456", "aaa1234567"})
    public void shouldPassUsernameIfItIsSpainPassport(String examplePossibleNumber) {
        // when
        String result = BbvaUtils.formatUsername(examplePossibleNumber);

        // then
        assertThat(result).isEqualTo(examplePossibleNumber);
    }

    @Test
    @Parameters({
        "AA123456",
        "aa123456",
    })
    public void shouldPassUsernameIfItIsForeignersPassport(String examplePossibleNumber) {
        // when
        String result = BbvaUtils.formatUsername(examplePossibleNumber);

        // then
        assertThat(result).isEqualTo(examplePossibleNumber);
    }
}
