package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.executors.updatepayment.rpc;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.executors.rpc.FromAccountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.UpcomingTransaction;

@JsonObject
public class ConfirmedTransactionsEntity {
    private String currencyCode;
    private String amount;
    private List<ConfirmedTransactionEntity> transactions;
    private FromAccountEntity fromAccount;

    public String getCurrencyCode() {
        return currencyCode;
    }

    public String getAmount() {
        return amount;
    }

    public List<ConfirmedTransactionEntity> getTransactions() {
        return transactions;
    }

    public FromAccountEntity getFromAccount() {
        return fromAccount;
    }

    public List<UpcomingTransaction> toTinkUpcomingTransactions() {
        return Optional.ofNullable(transactions).orElseGet(Collections::emptyList).stream()
                .map(transaction -> transaction.toTinkUpcomingTransaction(fromAccount.generalGetAccountIdentifier()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }
}
