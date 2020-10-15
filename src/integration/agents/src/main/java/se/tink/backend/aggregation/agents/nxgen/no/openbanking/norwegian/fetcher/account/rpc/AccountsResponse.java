package se.tink.backend.aggregation.agents.nxgen.no.openbanking.norwegian.fetcher.account.rpc;

import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.norwegian.fetcher.account.entities.AccountsItemEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccountsResponse {

    private List<AccountsItemEntity> accounts;

    public List<AccountsItemEntity> getAccounts() {
        return accounts;
    }
}
