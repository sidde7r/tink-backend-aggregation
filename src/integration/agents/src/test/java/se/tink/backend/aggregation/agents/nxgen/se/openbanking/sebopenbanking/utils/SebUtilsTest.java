package se.tink.backend.aggregation.agents.nxgen.se.openbanking.sebopenbanking.utils;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import se.tink.backend.aggregation.agents.exceptions.payment.CreditorValidationException;
import se.tink.backend.aggregation.agents.exceptions.payment.DebtorValidationException;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sebopenbanking.executor.payment.entities.CreditorAccountEntity;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sebopenbanking.executor.payment.entities.DebtorAccountEntity;

public class SebUtilsTest {

    private static final String IBAN_REQUIRED_PRODUCT = "sepa-credit-transfers";
    private static final String IBAN_ACCOUNT_EXAMPLE_SEB = "SE4550000000058398257466";
    private static final String NON_IBAN_ACCOUNT_NUMBER = "56240273996";

    @Test
    public void testFalseWhenAccountNumberNotIban() {
        assertFalse(
                SebUtils.isValidAccountForProduct(IBAN_REQUIRED_PRODUCT, NON_IBAN_ACCOUNT_NUMBER));
    }

    @Test
    public void testTrueWhenAccountNumberIban() {
        assertTrue(
                SebUtils.isValidAccountForProduct(IBAN_REQUIRED_PRODUCT, IBAN_ACCOUNT_EXAMPLE_SEB));
    }

    @Test(expected = CreditorValidationException.class)
    public void testCreditorValidationException() throws CreditorValidationException {
        CreditorAccountEntity.create(NON_IBAN_ACCOUNT_NUMBER, IBAN_REQUIRED_PRODUCT);
    }

    @Test(expected = DebtorValidationException.class)
    public void testDebtorValidationException() throws DebtorValidationException {
        DebtorAccountEntity.of(NON_IBAN_ACCOUNT_NUMBER);
    }

    @Test
    public void testValidAccountEntity()
            throws CreditorValidationException, DebtorValidationException {
        DebtorAccountEntity.of(IBAN_ACCOUNT_EXAMPLE_SEB);
        CreditorAccountEntity.create(IBAN_ACCOUNT_EXAMPLE_SEB, IBAN_REQUIRED_PRODUCT);
        // No Exception
        assertTrue(true);
    }
}
