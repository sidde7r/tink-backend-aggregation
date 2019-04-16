package se.tink.backend.aggregation.agents.nxgen.fi.banks.op.fetcher.rpc;

import static junit.framework.TestCase.assertEquals;

import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.op.fetcher.transactionalaccounts.entity.OpBankAccountEntity;
import se.tink.libraries.user.rpc.User;

public class OpBankAccountEntityTest {
    private static final String EXPECTED_ACCOUNT_NUMBER = "FI1234567890123456";
    private OpBankAccountEntity account;

    @Before
    public void setUp() throws Exception {
        account =
                new OpBankAccountEntity()
                        .setAccountNumber("FI1234567890123456")
                        .setBankingServiceTypeCode("710001");
    }

    @Test(expected = IllegalArgumentException.class)
    public void getNormalizedBankIdWhenEmpty() {
        account.setAccountNumber("");
        account.toTransactionalAccount();
    }

    @Test
    public void getNormalizedBankIdIllegalCharsRemoved() {
        account.setAccountNumber("-FI 12'34 567?890123456_");

        assertEquals(
                EXPECTED_ACCOUNT_NUMBER,
                account.toTransactionalAccount().toSystemAccount(new User()).getBankId());
    }

    @Test
    public void getAccountTypeNonExistingTypeCode() {
        account.setBankingServiceTypeCode("NON-EXISTING");

        assertEquals(AccountTypes.OTHER, account.toTransactionalAccount().getType());
    }

    @Test
    public void getAccountTypeChecking() {
        account.setBankingServiceTypeCode("710001");
        assertEquals(AccountTypes.CHECKING, account.toTransactionalAccount().getType());

        account.setBankingServiceTypeCode("710002");
        assertEquals(AccountTypes.CHECKING, account.toTransactionalAccount().getType());
    }

    @Test
    public void getAccountTypeSavings() {
        account.setBankingServiceTypeCode("712035");
        assertEquals(AccountTypes.SAVINGS, account.toTransactionalAccount().getType());

        account.setBankingServiceTypeCode("711030");
        assertEquals(AccountTypes.SAVINGS, account.toTransactionalAccount().getType());

        account.setBankingServiceTypeCode("712007");
        assertEquals(AccountTypes.SAVINGS, account.toTransactionalAccount().getType());

        account.setBankingServiceTypeCode("712008");
        assertEquals(AccountTypes.SAVINGS, account.toTransactionalAccount().getType());

        account.setBankingServiceTypeCode("712015");
        assertEquals(AccountTypes.SAVINGS, account.toTransactionalAccount().getType());
    }

    @Test
    public void getAccountTypeOther() {
        account.setBankingServiceTypeCode("710011");
        assertEquals(AccountTypes.OTHER, account.toTransactionalAccount().getType());

        account.setBankingServiceTypeCode("710012");
        assertEquals(AccountTypes.OTHER, account.toTransactionalAccount().getType());

        account.setBankingServiceTypeCode("710013");
        assertEquals(AccountTypes.OTHER, account.toTransactionalAccount().getType());

        account.setBankingServiceTypeCode("110001");
        assertEquals(AccountTypes.OTHER, account.toTransactionalAccount().getType());

        account.setBankingServiceTypeCode("120000");
        assertEquals(AccountTypes.OTHER, account.toTransactionalAccount().getType());

        account.setBankingServiceTypeCode("120001");
        assertEquals(AccountTypes.OTHER, account.toTransactionalAccount().getType());
    }
}
