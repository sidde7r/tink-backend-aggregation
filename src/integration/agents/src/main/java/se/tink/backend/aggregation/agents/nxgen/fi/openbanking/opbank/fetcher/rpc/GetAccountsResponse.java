package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.fetcher.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.fetcher.entities.AccountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class GetAccountsResponse {

    @JsonProperty("accounts")
    private List<AccountEntity> accountsList;

    public List<AccountEntity> getAccountsList() {
        return accountsList;
    }
}
