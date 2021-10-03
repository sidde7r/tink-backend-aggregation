package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.fetcher.transactionalaccount.rpc;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.entities.transactions.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.entities.transactions.TransactionsEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
public class TransactionResponse {

    private AccountEntity account;
    private TransactionsEntity transactions;

    public AccountEntity getAccount() {
        return account;
    }

    public void setAccount(AccountEntity account) {
        this.account = account;
    }

    public List<Transaction> getTransactions(Date limitDate) {
        return Objects.requireNonNull(Optional.ofNullable(transactions).orElse(null))
                .toTinkTransactions(limitDate);
    }

    public void setTransactions(TransactionsEntity transactions) {
        this.transactions = transactions;
    }

    public String getNextLink() {
        return Optional.ofNullable(transactions).map(TransactionsEntity::getNextLink).orElse(null);
    }
}
