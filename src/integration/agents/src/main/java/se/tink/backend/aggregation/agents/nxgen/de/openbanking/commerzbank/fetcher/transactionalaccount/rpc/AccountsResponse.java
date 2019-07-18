package se.tink.backend.aggregation.agents.nxgen.de.openbanking.commerzbank.fetcher.transactionalaccount.rpc;

import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.commerzbank.fetcher.transactionalaccount.entities.TransactionAccountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccountsResponse {

    private List<TransactionAccountEntity> accounts;

    public List<TransactionAccountEntity> getAccounts() {
        return accounts;
    }
}
