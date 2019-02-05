package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.executors.updatepayment.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Preconditions;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.SwedbankBasePredicates;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.UpcomingTransaction;
import se.tink.libraries.transfer.rpc.Transfer;
import se.tink.libraries.account.identifiers.SwedishIdentifier;

@JsonObject
public class PaymentsConfirmedResponse {
    private String currency;
    private String totalSum;
    private List<ConfirmedTransactionsEntity> confirmedTransactions;

    public String getCurrency() {
        return currency;
    }

    public String getTotalSum() {
        return totalSum;
    }

    public List<ConfirmedTransactionsEntity> getConfirmedTransactions() {
        return confirmedTransactions;
    }

    @JsonIgnore
    public Collection<UpcomingTransaction> toTinkUpcomingTransactions(String accountNumber) {
        Preconditions.checkNotNull(accountNumber, "Account number cannot be null.");

        return Optional.ofNullable(confirmedTransactions).orElseGet(Collections::emptyList).stream()
                .filter(cte -> Objects.equals(new SwedishIdentifier(accountNumber).getIdentifier(),
                        cte.getFromAccount().generalGetAccountIdentifier().getIdentifier()))
                .map(ConfirmedTransactionsEntity::toTinkUpcomingTransactions)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    @JsonIgnore
    public Optional<ConfirmedTransactionEntity> getConfirmedPayment(Transfer originalTransfer) {
        return Optional.ofNullable(confirmedTransactions).orElseGet(Collections::emptyList).stream()
                .filter(SwedbankBasePredicates.filterSourceAccount(originalTransfer))
                .map(ConfirmedTransactionsEntity::getTransactions)
                .flatMap(Collection::stream)
                .filter(SwedbankBasePredicates.FILTER_PAYMENTS)
                .filter(SwedbankBasePredicates.filterByAmount(originalTransfer, currency))
                .filter(SwedbankBasePredicates.filterByDate(originalTransfer))
                .filter(SwedbankBasePredicates.filterByDestinationAccount(originalTransfer))
                .filter(SwedbankBasePredicates.filterByMessage(originalTransfer))
                .findFirst();
    }
}
