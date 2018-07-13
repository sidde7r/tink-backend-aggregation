package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.rpc;

import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;
import se.tink.backend.aggregation.rpc.AccountTypes;

@JsonObject
public class SavingAccountEntity extends AccountEntity {
    private static final Logger log = LoggerFactory.getLogger(SavingAccountEntity.class);

    public Optional<TransactionalAccount> toTransactionalAccount(BankProfile bankProfile) {
        if (type != null) {
            // It seems as if the investment accounts has a type and the rest doesn't.
            log.info("Swedbank account type:[%s]", type);
            return Optional.empty();
        }

        return toTransactionalAccount(bankProfile, AccountTypes.SAVINGS);
    }
}
