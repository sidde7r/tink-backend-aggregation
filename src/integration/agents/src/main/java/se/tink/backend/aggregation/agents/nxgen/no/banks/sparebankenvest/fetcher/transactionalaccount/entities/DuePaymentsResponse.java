package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankenvest.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.UpcomingTransaction;

@JsonObject
public class DuePaymentsResponse extends ArrayList<PaymentEntity> {
    @JsonIgnore
    public Collection<UpcomingTransaction> getUpcomingTransactions() {
        return stream()
                .filter(PaymentEntity::isPaymentActive)
                .map(PaymentEntity::toTinkUpcomingTransaction)
                .collect(Collectors.toList());
    }
}
