package se.tink.backend.aggregation.nxgen.core.account.transactional;

import org.junit.Test;
import se.tink.backend.aggregation.nxgen.core.account.entity.HolderName;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.libraries.amount.Amount;
import static org.junit.Assert.assertEquals;

public class TransactionalAccountTest {

    private static final String ACCOUNT_NUMBER = "123456";

    @Test
    public void ensureBankIdentifierHasCorrectFormat() {
        TransactionalAccount transactionalAccount =
                TransactionalAccount.builder(AccountTypes.CHECKING, ACCOUNT_NUMBER, Amount.inSEK(1.0))
                        .setAccountNumber(ACCOUNT_NUMBER)
                        .setBankIdentifier("123456")
                        .setName("")
                        .build();

        assertEquals("123456", transactionalAccount.getBankIdentifier());
    }

    @Test
    public void checkDifferentOrderOfMethods() {
        TransactionalAccount.Builder<?, ?> transactionalBuilder =
                TransactionalAccount.builder(AccountTypes.CHECKING, ACCOUNT_NUMBER)
                        .setHolderName(new HolderName("name"))
                        .setAccountNumber(ACCOUNT_NUMBER)
                        .setBalance(Amount.inSEK(12d));
        transactionalBuilder.setBalance(Amount.inDKK(20d));
        TransactionalAccount transactionalAccount = transactionalBuilder.build();
        assertEquals(Amount.inDKK(20d), transactionalAccount.getBalance());
    }

    @Test
    public void otherAccountAsOther() {
        TransactionalAccount.Builder<?, ?> transactionalBuilder =
                TransactionalAccount.builder(AccountTypes.OTHER, ACCOUNT_NUMBER)
                        .setHolderName(new HolderName("name"))
                        .setAccountNumber(ACCOUNT_NUMBER)
                        .setBalance(Amount.inDKK(12d));
        TransactionalAccount transactionalAccount = transactionalBuilder.build();
        assertEquals(Amount.inDKK(12d), transactionalAccount.getBalance());
        assertEquals(AccountTypes.OTHER, transactionalAccount.getType());
    }

    @Test
    public void savingAccountBuilderRandomSettingOrder() {
        SavingsAccount savingsAccount =
                SavingsAccount.builder(ACCOUNT_NUMBER)
                        // setting additional data before general one
                        .setInterestRate(1d)
                        .setAccountNumber(ACCOUNT_NUMBER)
                        .setBalance(Amount.inEUR(1d))
                        // setting additional data after general one
                        .setInterestRate(2d)
                        .build();
        assertEquals(Double.valueOf(2d), savingsAccount.getInterestRate());
    }
}
