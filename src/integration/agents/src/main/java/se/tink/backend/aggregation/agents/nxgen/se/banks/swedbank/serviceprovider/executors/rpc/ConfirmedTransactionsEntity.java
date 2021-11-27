package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.executors.rpc;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.UpcomingTransaction;

@JsonObject
@Getter
public class ConfirmedTransactionsEntity {
    private String currencyCode;
    private String amount;
    private List<ConfirmedTransactionEntity> transactions;
    private FromAccountEntity fromAccount;

    public List<UpcomingTransaction> toTinkUpcomingTransactions() {
        return Optional.ofNullable(transactions).orElseGet(Collections::emptyList).stream()
                .map(
                        transaction ->
                                transaction.toTinkUpcomingTransaction(
                                        fromAccount.generalGetAccountIdentifier()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }
}
