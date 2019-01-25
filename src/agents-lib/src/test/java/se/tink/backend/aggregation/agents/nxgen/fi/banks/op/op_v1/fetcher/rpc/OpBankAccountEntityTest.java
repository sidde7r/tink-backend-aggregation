package se.tink.backend.aggregation.agents.nxgen.fi.banks.op.op_v1.fetcher.rpc;

import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.op.fetcher.entities.OpBankAccountEntity;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.agents.rpc.User;

import static junit.framework.TestCase.assertEquals;

public class OpBankAccountEntityTest {
    private static final String EXPECTED_ACCOUNT_NUMBER = "FI1234567890123456";
    private OpBankAccountEntity account;

    @Before
    public void setUp() throws Exception {
        account = new OpBankAccountEntity().setAccountNumber("FI1234567890123456").setTypeCode("710001");
    }

    @Test
    public void getNormalizedBankId() {
        assertEquals(EXPECTED_ACCOUNT_NUMBER, account.toTransactionalAccount().getBankIdentifier());
    }

    @Test(expected = IllegalArgumentException.class)
    public void getNormalizedBankIdWhenEmpty() {
        account.setAccountNumber("");
        account.toTransactionalAccount();
    }

    @Test
    public void getNormalizedBankIdIllegalCharsRemoved() {
        account.setAccountNumber("-FI 12'34 567?890123456_");

        assertEquals(EXPECTED_ACCOUNT_NUMBER, account.toTransactionalAccount().toSystemAccount(new User()).getBankId());
    }

    @Test
    public void getAccountTypeNonExistingTypeCode() {
        account.setTypeCode("NON-EXISTING");

        assertEquals(AccountTypes.CHECKING, account.toTransactionalAccount().getType());
    }

    @Test
    public void getAccountTypeChecking() {
        account.setTypeCode("710001");
        assertEquals(AccountTypes.CHECKING, account.toTransactionalAccount().getType());

        account.setTypeCode("710002");
        assertEquals(AccountTypes.CHECKING, account.toTransactionalAccount().getType());
    }

    @Test
    public void getAccountTypeSavings() {
        account.setTypeCode("712035");
        assertEquals(AccountTypes.SAVINGS, account.toTransactionalAccount().getType());

        account.setTypeCode("711030");
        assertEquals(AccountTypes.SAVINGS, account.toTransactionalAccount().getType());

        account.setTypeCode("712007");
        assertEquals(AccountTypes.SAVINGS, account.toTransactionalAccount().getType());

        account.setTypeCode("712008");
        assertEquals(AccountTypes.SAVINGS, account.toTransactionalAccount().getType());

        account.setTypeCode("712015");
        assertEquals(AccountTypes.SAVINGS, account.toTransactionalAccount().getType());
    }

    @Test(expected = IllegalStateException.class)
    public void ensureTransactionalAccounts_cantBeOfTypePension() {
        account.setTypeCode("711037");

        assertEquals(AccountTypes.PENSION, account.toTransactionalAccount().getType());
    }

    @Test
    public void getAccountTypeOther() {
        account.setTypeCode("710011");
        assertEquals(AccountTypes.CHECKING, account.toTransactionalAccount().getType());

        account.setTypeCode("710012");
        assertEquals(AccountTypes.CHECKING, account.toTransactionalAccount().getType());

        account.setTypeCode("710013");
        assertEquals(AccountTypes.CHECKING, account.toTransactionalAccount().getType());

        account.setTypeCode("712050");
        assertEquals(AccountTypes.CHECKING, account.toTransactionalAccount().getType());

        account.setTypeCode("110001");
        assertEquals(AccountTypes.CHECKING, account.toTransactionalAccount().getType());

        account.setTypeCode("120000");
        assertEquals(AccountTypes.CHECKING, account.toTransactionalAccount().getType());

        account.setTypeCode("120001");
        assertEquals(AccountTypes.CHECKING, account.toTransactionalAccount().getType());
    }
}
