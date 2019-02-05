package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.rpc;

import java.util.Optional;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;
import se.tink.backend.agents.rpc.AccountTypes;

@JsonObject
public class TransactionalAccountEntity extends AccountEntity {
    private String originalName;

    public String getOriginalName() {
        return originalName;
    }

    public Optional<TransactionalAccount> toTransactionalAccount(BankProfile bankProfile) {
        return toTransactionalAccount(bankProfile, AccountTypes.CHECKING);
    }
}
