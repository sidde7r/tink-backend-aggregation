package se.tink.backend.aggregation.nxgen.core.account.transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

import org.junit.Test;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.nxgen.core.account.entity.HolderName;
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

    @Test
    public void checkDifferentOrderOfMethods() {
        TransactionalAccount.Builder<?, ?> transactionalBuilder =
                TransactionalAccount.builder(AccountTypes.CHECKING, ACCOUNT_NUMBER)
                        .setHolderName(new HolderName("name"))
                        .setAccountNumber(ACCOUNT_NUMBER)
                        .setExactBalance(ExactCurrencyAmount.of(12, "DKK"));
        transactionalBuilder.setExactBalance(ExactCurrencyAmount.of(20, "DKK"));
        TransactionalAccount transactionalAccount = transactionalBuilder.build();
        assertThat(transactionalAccount.getExactBalance().getDoubleValue()).isEqualTo(20);
    }

    @Test
    public void otherAccountAsOther() {
        TransactionalAccount.Builder<?, ?> transactionalBuilder =
                TransactionalAccount.builder(AccountTypes.OTHER, ACCOUNT_NUMBER)
                        .setHolderName(new HolderName("name"))
                        .setAccountNumber(ACCOUNT_NUMBER)
                        .setExactBalance(ExactCurrencyAmount.of(12, "DKK"));
        TransactionalAccount transactionalAccount = transactionalBuilder.build();
        assertThat(transactionalAccount.getExactBalance())
                .isEqualTo(ExactCurrencyAmount.of(12, "DKK"));
        assertThat(transactionalAccount.getType()).isEqualTo(AccountTypes.OTHER);
    }

    @Test
    public void savingAccountBuilderRandomSettingOrder() {
        SavingsAccount savingsAccount =
                SavingsAccount.builder(ACCOUNT_NUMBER)
                        // setting additional data before general one
                        .setInterestRate(1d)
                        .setAccountNumber(ACCOUNT_NUMBER)
                        .setExactBalance(ExactCurrencyAmount.of(1, "DKK"))
                        // setting additional data after general one
                        .setInterestRate(2d)
                        .build();
        assertThat(savingsAccount.getInterestRate()).isEqualTo(2d);
    }
}
