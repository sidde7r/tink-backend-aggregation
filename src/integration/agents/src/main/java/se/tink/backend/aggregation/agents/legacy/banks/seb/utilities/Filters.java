package se.tink.backend.aggregation.agents.banks.seb.utilities;

import com.google.common.base.Objects;
import com.google.common.base.Predicate;
import se.tink.backend.aggregation.agents.banks.seb.model.AccountEntity;
import se.tink.backend.aggregation.agents.banks.seb.model.ExternalAccount;

public class Filters {

    public static Predicate<AccountEntity> accountWithAccountNumber(final String accountNumber) {
        return entity -> Objects.equal(entity.KONTO_NR, accountNumber);
    }

    public static Predicate<ExternalAccount> externalAccountWithAccountNumber(
            final String accountNumber) {
        return entity -> {
            // Ignore bank and giro accounts since we don't support doing transfers to and from them
            return !entity.isBankGiro()
                    && !entity.isPostGiro()
                    && Objects.equal(entity.DestinationNumber, accountNumber);
        };
    }
}
