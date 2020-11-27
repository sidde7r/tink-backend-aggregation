package se.tink.backend.aggregation.workers.commands.migrations.implementations.banks.nordeadk;

import org.assertj.core.api.AbstractAssert;
import org.assertj.core.util.Preconditions;
import org.junit.Ignore;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.AccountTypes;

@Ignore
public class NordeaMigrationAccountAssertion
        extends AbstractAssert<NordeaMigrationAccountAssertion, Account> {

    public NordeaMigrationAccountAssertion(Account account) {
        super(account, NordeaMigrationAccountAssertion.class);
    }

    public static NordeaMigrationAccountAssertion assertThat(Account actual) {
        return new NordeaMigrationAccountAssertion(actual);
    }

    public NordeaMigrationAccountAssertion hasType(AccountTypes type) {
        isNotNull();
        if (actual.getType() != type) {
            failWithMessage(
                    "Expected account's type to be <%s> but was <%s>", type, actual.getType());
        }
        return this;
    }

    public NordeaMigrationAccountAssertion isCheckingAccount() {
        return hasType(AccountTypes.CHECKING);
    }

    public NordeaMigrationAccountAssertion isCreditCard() {
        return hasType(AccountTypes.CREDIT_CARD);
    }

    public NordeaMigrationAccountAssertion hasBankId(String bankId) {
        isNotNull();
        Preconditions.checkNotNull(bankId);
        if (!bankId.equals(actual.getBankId())) {
            failWithMessage(
                    "Expected account's bankId to be <%s> but was <%s>",
                    bankId, actual.getBankId());
        }
        return this;
    }

    public NordeaMigrationAccountAssertion hasAccountNumber(String hasAccountNumber) {
        isNotNull();
        Preconditions.checkNotNull(hasAccountNumber);
        if (!hasAccountNumber.equals(actual.getAccountNumber())) {
            failWithMessage(
                    "Expected account's account number to be <%s> but was <%s>",
                    hasAccountNumber, actual.getBankId());
        }
        return this;
    }
}
