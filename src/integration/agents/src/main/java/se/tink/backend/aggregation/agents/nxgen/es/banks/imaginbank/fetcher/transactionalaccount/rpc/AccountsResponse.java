package se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.fetcher.transactionalaccount.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.fetcher.transactionalaccount.entities.AccountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccountsResponse {

    @JsonProperty("listaCuentas")
    private ListAccountsResponse listAccountsResponse;

    public List<AccountEntity> getAccounts() {
        return listAccountsResponse.getAccounts();
    }

    public boolean isMoreData() {
        return listAccountsResponse.isMoreData();
    }
}
