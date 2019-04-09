package se.tink.backend.aggregation.agents.banks.se.icabanken.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.Objects;
import se.tink.backend.aggregation.agents.models.Transaction;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UpcomingTransactionsBody {
    @JsonProperty("Assignments")
    private List<UpcomingTransactionEntity> upcomingTransactions;

    public List<UpcomingTransactionEntity> getUpcomingTransactions() {
        return upcomingTransactions;
    }

    public void setUpcomingTransactions(List<UpcomingTransactionEntity> upcomingTransactions) {
        this.upcomingTransactions = upcomingTransactions;
    }

    public List<Transaction> findTransactionsFor(AccountEntity accountEntity) {
        List<Transaction> transactions = Lists.newArrayList();

        for (UpcomingTransactionEntity upcomingTransactionEntity : upcomingTransactions) {
            if (Objects.equals(
                    upcomingTransactionEntity.getAccountId(), accountEntity.getAccountId())) {
                Transaction transaction = upcomingTransactionEntity.toTransaction(true);

                transactions.add(transaction);
            }
        }

        return transactions;
    }

    public List<UpcomingTransactionEntity> findUpcomingTransactionsFor(
            AccountEntity accountEntity) {
        List<UpcomingTransactionEntity> upcomingTransactions = Lists.newArrayList();

        for (UpcomingTransactionEntity upcomingTransactionEntity : upcomingTransactions) {
            if (Objects.equals(
                    upcomingTransactionEntity.getAccountId(), accountEntity.getAccountId())) {
                upcomingTransactions.add(upcomingTransactionEntity);
            }
        }

        return upcomingTransactions;
    }
}
