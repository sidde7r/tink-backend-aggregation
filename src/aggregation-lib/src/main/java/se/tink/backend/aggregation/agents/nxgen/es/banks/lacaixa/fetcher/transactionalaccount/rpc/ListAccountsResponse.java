package se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.transactionalaccount.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collection;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.transactionalaccount.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.transactionalaccount.entities.AccountList;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.transactionalaccount.entities.TransactionList;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.entity.HolderName;

@JsonObject
public class ListAccountsResponse {

    @JsonProperty("listaCuentas")
    AccountList accountList;

    @JsonProperty("listaMovimientos")
    TransactionList transactions;

    @JsonIgnore
    public Collection<AccountEntity> getAccounts() {
        return accountList.getAccounts();
    }

    public Collection<TransactionalAccount> getTransactionalAccounts(HolderName holderName){
        return accountList.getAccounts().stream()
                .map(account -> account.toTinkAccount(holderName))
                .collect(Collectors.toList());
    }

    @JsonIgnore
    public boolean hasAccounts(){
        return !accountList.isEmpty();
    }
}
