package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.handelsbanken.fetcher;

import org.assertj.core.api.AbstractAssert;
import org.junit.Ignore;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.libraries.amount.ExactCurrencyAmount;

@Ignore
public class TransactionalAccountAsserts
        extends AbstractAssert<TransactionalAccountAsserts, TransactionalAccount> {

    public TransactionalAccountAsserts(TransactionalAccount account) {
        super(account, TransactionalAccountAsserts.class);
    }

    public static TransactionalAccountAsserts assertThat(TransactionalAccount actual) {
        return new TransactionalAccountAsserts(actual);
    }

    public TransactionalAccountAsserts isOfType(AccountTypes type) {
        if (!actual.getType().equals(type)) {
            failWithMessage(
                    "Expected account's type to be <%s> but was <%s>", type, actual.getType());
        }
        return this;
    }

    public TransactionalAccountAsserts isCheckingAccount() {
        return isOfType(AccountTypes.CHECKING);
    }

    public TransactionalAccountAsserts hasAccountNumber(String accountNumber) {
        if (!actual.getAccountNumber().equals(accountNumber)) {
            failWithMessage(
                    "Expected account number to be <%s>, but was <%s>",
                    accountNumber, actual.getAccountNumber());
        }
        return this;
    }

    public TransactionalAccountAsserts hasHolder(String holder) {
        if (!actual.getHolderName().toString().equals(holder)) {
            failWithMessage(
                    "Expected account's holder to be <%s>, but was <%s>",
                    holder, actual.getHolderName().toString());
        }
        return this;
    }

    public TransactionalAccountAsserts hasBalance(ExactCurrencyAmount availableBalance) {
        if (!actual.getExactBalance().equals(availableBalance)) {
            failWithMessage(
                    "Expected available balance to be <%s>, but was <%s>",
                    availableBalance, actual.getExactAvailableBalance());
        }
        return this;
    }
}
