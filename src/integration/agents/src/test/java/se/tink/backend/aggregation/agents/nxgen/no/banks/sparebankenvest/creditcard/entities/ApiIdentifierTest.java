package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankenvest.creditcard.entities;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankenvest.fetcher.creditcard.entities.ApiIdentifier;

public class ApiIdentifierTest {
    @Test
    public void testCreateApiIdentifier() throws Exception {
        ApiIdentifier apiIdentifier = new ApiIdentifier("cardNumberGuid", "kidGuid");

        assertEquals("cardNumberGuid\nkidGuid", apiIdentifier.getApiIdentifier());
    }

    @Test
    public void testParseApiIdentifier() throws Exception {
        ApiIdentifier apiIdentifier = new ApiIdentifier("cardNumberGuid\nkidGuid");

        assertEquals("cardNumberGuid", apiIdentifier.getCardNumberGuid());
        assertEquals("kidGuid", apiIdentifier.getKidGuid());
    }
}
