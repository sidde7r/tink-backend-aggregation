package se.tink.backend.aggregation.agents.nxgen.de.banks.santander.fetcher.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collection;
import se.tink.backend.aggregation.agents.nxgen.de.banks.santander.fetcher.entities.AccountResultEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;

@JsonObject
public class FetchAccountsResponse {
    @JsonProperty("methodResult")
    private AccountResultEntity result;

    public Collection<TransactionalAccount> toTransactionalAccounts(){
        return result.toTransactionalAccount();
    }

    public AccountResultEntity getAccountResultEntity() {
        return result;
    }
}
