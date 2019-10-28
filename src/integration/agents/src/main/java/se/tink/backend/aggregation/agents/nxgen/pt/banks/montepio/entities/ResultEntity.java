package se.tink.backend.aggregation.agents.nxgen.pt.banks.montepio.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.montepio.fetcher.accounts.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.montepio.fetcher.accounts.entities.TransactionEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ResultEntity {

    @JsonProperty("HasMorePages")
    private Boolean hasMorePages;

    @JsonProperty("CustomerProducts")
    private List<AccountEntity> accounts;

    @JsonProperty("AccountTransactions")
    private List<TransactionEntity> transactions;

    public Optional<List<AccountEntity>> getAccounts() {
        return Optional.ofNullable(accounts);
    }

    public Optional<List<TransactionEntity>> getTransactions() {
        return Optional.ofNullable(transactions);
    }

    public boolean hasMorePages() {
        return Boolean.TRUE.equals(hasMorePages);
    }
}
