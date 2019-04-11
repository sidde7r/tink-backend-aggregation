package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.utils;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;

import io.vavr.control.Option;
import org.junit.Test;

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
}
