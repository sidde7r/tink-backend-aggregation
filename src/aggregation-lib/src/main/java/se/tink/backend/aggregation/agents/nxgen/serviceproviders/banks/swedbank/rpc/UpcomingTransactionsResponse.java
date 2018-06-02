package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.rpc;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.SwedbankBasePredicates;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.UpcomingTransaction;

@JsonObject
public class UpcomingTransactionsResponse {
    private int numberOfTransactions;
    private AmountEntity totalAmount;
    private List<UpcomingTransactionEntity> upcomingTransactions;
    private AmountEntity totalAmountSigned;
    private AmountEntity totalAmountUnsigned;
    private int numberOfTransactionsUnsigned;
    private int numberOfTransactionsSigned;

    public int getNumberOfTransactions() {
        return numberOfTransactions;
    }

    public AmountEntity getTotalAmount() {
        return totalAmount;
    }

    public List<UpcomingTransactionEntity> getUpcomingTransactions() {
        return upcomingTransactions;
    }

    public AmountEntity getTotalAmountSigned() {
        return totalAmountSigned;
    }

    public AmountEntity getTotalAmountUnsigned() {
        return totalAmountUnsigned;
    }

    public int getNumberOfTransactionsUnsigned() {
        return numberOfTransactionsUnsigned;
    }

    public int getNumberOfTransactionsSigned() {
        return numberOfTransactionsSigned;
    }

    public List<UpcomingTransaction> toTinkUpcomingTransactions(String accountNumber, String defaultCurrency) {
        if (upcomingTransactions == null) {
            return Collections.emptyList();
        }

        return upcomingTransactions.stream()
                .filter(SwedbankBasePredicates.filterAccounts(accountNumber))
                .map(upcomingTransactionEntity -> upcomingTransactionEntity.toTinkUpcomingTransaction(defaultCurrency))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }
}
