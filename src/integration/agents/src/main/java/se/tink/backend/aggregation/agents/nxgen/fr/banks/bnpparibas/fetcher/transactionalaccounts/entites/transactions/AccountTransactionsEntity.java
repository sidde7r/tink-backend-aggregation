package se.tink.backend.aggregation.agents.nxgen.fr.banks.bnpparibas.fetcher.transactionalaccounts.entites.transactions;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
public class AccountTransactionsEntity {
    @JsonProperty("dateSoldeDispo")
    private String dateBalanceAvailable;
    @JsonProperty("devise")
    private String currency;
    private String key;
    @JsonProperty("nbJourDebiteur")
    private int debtorDay;
    @JsonProperty("operationPassee")
    private List<TransactionEntity> transactionList;
    @JsonProperty("soldeAVenir")
    private double soldeaVenir;
    @JsonProperty("soldeDispo")
    private double disposableBalance;

    @JsonIgnore
    public Collection<Transaction> toTinkTransactions(){
        return Optional.ofNullable(transactionList).orElseGet(Collections::emptyList)
                .stream()
                .map(TransactionEntity::toTinkTransaction)
                .collect(Collectors.toList());
    }

    public String getDateBalanceAvailable() {
        return dateBalanceAvailable;
    }

    public String getCurrency() {
        return currency;
    }

    public String getKey() {
        return key;
    }

    public int getDebtorDay() {
        return debtorDay;
    }

    public List<TransactionEntity> getTransactionList() {
        return transactionList;
    }

    public double getSoldeaVenir() {
        return soldeaVenir;
    }

    public double getDisposableBalance() {
        return disposableBalance;
    }
}
