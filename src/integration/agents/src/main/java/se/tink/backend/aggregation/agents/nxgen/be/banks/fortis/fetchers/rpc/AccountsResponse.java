package se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.fetchers.rpc;

import java.util.Collection;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.fetchers.entities.BusinessMessageBulk;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.fetchers.entities.Value;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

@JsonObject
public class AccountsResponse {
    private BusinessMessageBulk businessMessageBulk;
    private Value value;

    public Collection<TransactionalAccount> toTinkAccounts() {
        return value.toTinkAccounts();
    }

    public BusinessMessageBulk getBusinessMessageBulk() {
        return businessMessageBulk;
    }
}
