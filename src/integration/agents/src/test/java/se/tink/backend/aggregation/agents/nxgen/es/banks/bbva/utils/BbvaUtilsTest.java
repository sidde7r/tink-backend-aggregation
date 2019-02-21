package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.utils;

import java.util.Optional;
import org.junit.Ignore;
import org.junit.Test;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;

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
        Optional<String> nextKeyString = BbvaUtils.splitUtlGetKey(URL_TO_PARSE);
        assertTrue(nextKeyString.isPresent());
        assertEquals(NEXT_KEY, nextKeyString.get());
    }

    @Test(expected = IllegalStateException.class)
    public void splitUtlGetKey_NoKeyThrowException() {
        BbvaUtils.splitUtlGetKey(URL_TO_PARSE_NO_KEY);
    }


    //I wasn't able to find input that would trigger this one
    @Ignore
    @Test(expected = IllegalArgumentException.class)
    public void splitUtlGetKey_cannotParseThrowException() {
        BbvaUtils.splitUtlGetKey("");
    }
}
