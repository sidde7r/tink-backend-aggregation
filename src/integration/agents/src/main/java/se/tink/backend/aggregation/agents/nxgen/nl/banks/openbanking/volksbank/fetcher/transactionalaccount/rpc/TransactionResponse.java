package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.fetcher.transactionalaccount.rpc;

import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.entities.transactions.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.entities.transactions.TransactionsEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

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

    public TransactionsEntity getTransactions() {
        return transactions;
    }

    public void setTransactions(TransactionsEntity transactions) {
        this.transactions = transactions;
    }
}
