package se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.investment.entities;

import static org.junit.Assert.*;

import org.junit.Test;

public class ValueAccountIdentifierEntityTest {

    private static String TEST_ID = "01234567899876543210";

    @Test
    public void fromInternalProductCode() {
        ValueAccountIdentifierEntity identifier =
                ValueAccountIdentifierEntity.fromInternalProductCode(TEST_ID);

        assertEquals("0123", identifier.getEntity());
        assertEquals("4567", identifier.getCenter());
        assertEquals("89", identifier.getControlDigits());
        assertEquals("9876543210", identifier.getAccountNumber());
    }
}
