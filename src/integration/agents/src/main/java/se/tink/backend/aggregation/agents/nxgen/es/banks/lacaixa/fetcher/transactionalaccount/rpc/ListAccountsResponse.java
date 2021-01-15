package se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.transactionalaccount.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collection;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.transactionalaccount.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.transactionalaccount.entities.AccountList;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ListAccountsResponse {

    @JsonProperty("listaCuentas")
    private AccountList accountList;

    public Collection<AccountEntity> getAccounts() {
        return accountList.getAccounts();
    }

    public boolean hasAccounts() {
        return !accountList.isEmpty();
    }
}
