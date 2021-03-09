package se.tink.backend.aggregation.nxgen.core.account.transactional;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.libraries.amount.ExactCurrencyAmount;

public class TransactionalAccountTest {

    private static final String ACCOUNT_NUMBER = "123456";

    @Test
    public void ensureBankIdentifierHasCorrectFormat() {
        TransactionalAccount transactionalAccount =
                TransactionalAccount.builder(
                                AccountTypes.CHECKING,
                                ACCOUNT_NUMBER,
                                ExactCurrencyAmount.inSEK(1.0))
                        .setAccountNumber(ACCOUNT_NUMBER)
                        .setBankIdentifier("123456")
                        .setName("")
                        .build();

        assertEquals("123456", transactionalAccount.getApiIdentifier());
    }
}
