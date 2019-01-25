package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.transactionalaccount.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.transactionalaccount.entities.AccountContractsEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.Amount;

@JsonObject
public class AccountBalanceResponse {
    @JsonProperty("accountTransactions")
    private List<AccountContractsEntity> accounts;

    @JsonIgnore
    public Optional<Amount> getTinkAmountForId(String id) {
        if (Objects.isNull(accounts)) {
            return Optional.empty();
        }

        return accounts.stream()
                .filter(account -> account.isContractId(id))
                .map(AccountContractsEntity::getAvailableBalanceAsTinkAmount)
                .findFirst();
    }
}
