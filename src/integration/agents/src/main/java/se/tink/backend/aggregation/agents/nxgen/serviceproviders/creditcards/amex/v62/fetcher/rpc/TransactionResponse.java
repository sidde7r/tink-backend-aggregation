package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.fetcher.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.AmericanExpressV62Configuration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.fetcher.entities.ActivityListEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.fetcher.entities.TransactionDetailsEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.fetcher.entities.ValuesItem;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
public class TransactionResponse {
    private TransactionDetailsEntity transactionDetails;

    public TransactionDetailsEntity getTransactionDetails() {
        return this.transactionDetails;
    }

    @JsonIgnore
    public boolean isOkResponse() {
        return transactionDetails.getStatus() == 0;
    }

    @JsonIgnore
    private boolean hasTransactions() {
        int numTransctions = 0;
        if (hasMoreTransactions()) {
            for (ActivityListEntity activityListEntity : transactionDetails.getActivityList()) {
                if (activityListEntity.getTransactionList() != null) {
                    numTransctions += activityListEntity.getTransactionList().size();
                }
            }
        }

        return numTransctions > 0;
    }

    // if there is no activityList we will not be able to fetch more transactions
    @JsonIgnore
    private boolean hasMoreTransactions() {
        return transactionDetails.getActivityList() != null;
    }

    @JsonIgnore
    public boolean canFetchMore() {
        return hasMoreTransactions() && hasTransactions();
    }

    @JsonIgnore
    public List<Transaction> toTinkTransactions(
            final AmericanExpressV62Configuration config,
            final boolean isPending,
            final String suppIndex) {
        List<Transaction> transactions = new ArrayList<>();
        transactionDetails
                .getActivityList()
                .forEach(
                        activity ->
                                transactions.addAll(
                                        activity.getTransactions(config, isPending, suppIndex)));
        return transactions;
    }

    /**
     * Fetches the suppIndex for an account. In the response, each account is assigned an index
     * (suppIndex) to connect the account with a transaction. We have to check the transaction
     * details for the suppIndex of the account by mapping the holderName.
     *
     * @param account
     * @return
     */
    public String getSuppIndexForAccount(final CreditCardAccount account) {
        return transactionDetails.getFilterOptions().getCardmembers().getValues().stream()
                .filter(v -> v.getLabel().equalsIgnoreCase(account.getHolderName().toString()))
                .map(ValuesItem::getType)
                .findAny()
                .orElseThrow(NoSuchElementException::new);
    }
}
