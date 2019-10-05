package se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.investment.entities;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ValueAccountIdentifierEntityTest {

    private static String ENTITY = "0123";
    private static String CENTER = "4567";
    private static String CONTROL_DIGITS = "89";
    private static String ACCOUNT_NUMBER = "9876543210";

    private static String TEST_ID = ENTITY + CENTER + CONTROL_DIGITS + ACCOUNT_NUMBER;

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
