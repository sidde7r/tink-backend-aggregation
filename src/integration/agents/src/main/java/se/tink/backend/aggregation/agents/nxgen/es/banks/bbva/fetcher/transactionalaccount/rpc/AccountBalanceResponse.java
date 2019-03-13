package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.transactionalaccount.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.vavr.collection.List;
import io.vavr.control.Option;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.transactionalaccount.entities.AccountContractsEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.Amount;

@JsonObject
public class AccountBalanceResponse {
    @JsonProperty("accountTransactions")
    private List<AccountContractsEntity> accounts;

    @JsonIgnore
    public Option<Amount> getTinkAmountForId(String id) {
        return Option.of(accounts)
                .getOrElse(List.empty())
                .filter(account -> account.isContractId(id))
                .map(AccountContractsEntity::getAvailableBalanceAsTinkAmount)
                .headOption();
    }
}
