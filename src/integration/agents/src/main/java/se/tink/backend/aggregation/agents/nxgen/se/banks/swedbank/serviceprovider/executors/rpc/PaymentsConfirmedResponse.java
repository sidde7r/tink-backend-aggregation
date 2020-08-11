package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.executors.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Preconditions;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.UpcomingTransaction;
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
                .filter(
                        cte ->
                                Objects.equals(
                                        new SwedishIdentifier(accountNumber).getIdentifier(),
                                        cte.getFromAccount()
                                                .generalGetAccountIdentifier()
                                                .getIdentifier()))
                .map(ConfirmedTransactionsEntity::toTinkUpcomingTransactions)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }
}
