package se.tink.backend.aggregation.agents.nxgen.de.openbanking.commerzbank.fetcher.transactionalaccount.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collection;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.commerzbank.fetcher.transactionalaccount.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.commerzbank.fetcher.transactionalaccount.entities.LinkEntity;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.commerzbank.fetcher.transactionalaccount.entities.TransactionsEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
public class TransactionsResponse {

    @JsonProperty("_links")
    private LinkEntity links;

    private TransactionsEntity transactions;
    private AccountEntity account;

    public LinkEntity getLinks() {
        return links;
    }

    public TransactionsEntity getTransactions() {
        return transactions;
    }

    public AccountEntity getAccount() {
        return account;
    }

    @JsonIgnore
    public Collection<? extends Transaction> toTinkTransactions() {
        return Optional.ofNullable(transactions)
                .orElse(new TransactionsEntity())
                .toTinkTransactions();
    }
}
