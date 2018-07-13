package se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.transactions.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import se.tink.backend.aggregation.agents.banks.se.icabanken.model.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.Accounts.entities.OwnAccountsEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.system.rpc.Transaction;
import static com.google.api.client.util.Lists.newArrayList;

@JsonObject
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
        List<Transaction> transactions = new ArrayList<>();

        for (UpcomingTransactionEntity upcomingTransactionEntity : upcomingTransactions) {
            if (Objects.equals(upcomingTransactionEntity.getAccountId(), accountEntity.getAccountId())) {
                Transaction transaction = upcomingTransactionEntity.toTransaction(true);

                transactions.add(transaction);
            }
        }

        return transactions;
    }

    public List<UpcomingTransactionEntity> findUpcomingTransactionsFor(OwnAccountsEntity accountEntity) {
        List<UpcomingTransactionEntity> upcomingTransactions = new ArrayList<>();

        for (UpcomingTransactionEntity upcomingTransactionEntity : upcomingTransactions) {
            if (Objects.equals(upcomingTransactionEntity.getAccountId(), accountEntity.getAccountId())) {
                upcomingTransactions.add(upcomingTransactionEntity);
            }
        }

        return upcomingTransactions;
    }
}
