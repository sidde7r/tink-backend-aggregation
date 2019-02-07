package se.tink.backend.aggregation.nxgen.core.account;

import java.util.List;
import org.mockito.Mockito;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.SavingsAccount;
import se.tink.libraries.amount.Amount;
import se.tink.libraries.account.AccountIdentifier;

public class TestAccountBuilder<T extends Account> {
    private static final String ACCOUNT_NUMBER = "0123456789";
    private static final String BANK_ACCOUNT_ID = "Banks internal accountId";
    private static final String TINK_ID = "Unique id for tink";

    private T account;

    private TestAccountBuilder(Class<T> cls) {
        this.account = Mockito.mock(cls);
        Mockito.when(account.getType()).thenCallRealMethod();
    }

    public static <T extends Account> TestAccountBuilder<T> from(Class<T> accountType) {
        return new TestAccountBuilder<>(accountType);
    }

    public TestAccountBuilder setName(String name) {
        Mockito.when(account.getName()).thenReturn(name);
        return this;
    }

    public TestAccountBuilder setAccountNumber(String accountNumber) {
        Mockito.when(account.getAccountNumber()).thenReturn(accountNumber);
        return this;
    }

    public TestAccountBuilder setBalance(Amount balance) {
        Mockito.when(account.getBalance()).thenReturn(balance);
        return this;
    }

    public TestAccountBuilder setIdentifiers(List<AccountIdentifier> identifiers) {
        Mockito.when(account.getIdentifiers()).thenReturn(identifiers);
        return this;
    }

    public TestAccountBuilder setBankIdentifier(String bankIdentifier) {
        Mockito.when(account.getBankIdentifier()).thenReturn(bankIdentifier);
        return this;
    }

    public TestAccountBuilder setInterestRate(Double interestRate) {
        if (account instanceof LoanAccount) {
            Mockito.when(((LoanAccount) account).getInterestRate()).thenReturn(interestRate);
        } else if (account instanceof SavingsAccount) {
            Mockito.when(((SavingsAccount) account).getInterestRate()).thenReturn(interestRate);
        }

        return this;
    }

    public TestAccountBuilder setAvailableCredit(Amount availableCredit) {
        if (account instanceof CreditCardAccount) {
            Mockito.when(((CreditCardAccount) account).getAvailableCredit()).thenReturn(availableCredit);
        }

        return this;
    }

    public T build() {
        return account;
    }
}
