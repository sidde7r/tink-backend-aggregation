package se.tink.backend.aggregation.agents.nxgen.se.business.nordea.fetcher.transactionalaccount.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.se.business.nordea.fetcher.transactionalaccount.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.se.business.nordea.fetcher.transactionalaccount.entities.AccountsEntity;
import se.tink.backend.aggregation.agents.nxgen.se.business.nordea.rpc.NordeaSEResponse;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class FetchAccountsResponse extends NordeaSEResponse {
    @JsonProperty("getInitialContextOut")
    private AccountsEntity accountsEntity;

    public List<AccountEntity> getAccountsList() {
        return Optional.ofNullable(accountsEntity).orElse(new AccountsEntity()).getAccountsList();
    }
}
