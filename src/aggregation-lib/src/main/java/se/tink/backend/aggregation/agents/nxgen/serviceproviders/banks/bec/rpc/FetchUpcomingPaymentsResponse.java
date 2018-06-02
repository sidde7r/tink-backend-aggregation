package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.accounts.checking.entities.PaymentsEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.UpcomingTransaction;

@JsonObject
public class FetchUpcomingPaymentsResponse {
    private boolean browsePossible;
    private String nextPageId;
    private List<PaymentsEntity> payments;

    public boolean isBrowsePossible() {
        return browsePossible;
    }

    public String getNextPageId() {
        return nextPageId;
    }

    public List<PaymentsEntity> getPayments() {
        return payments;
    }

    @JsonIgnore
    public Collection<UpcomingTransaction> getTinkUpcomingTransactions() {
        if (payments == null) {
            return Collections.emptyList();
        }

        return payments.stream()
                .map(PaymentsEntity::toTinkUpcomingTransaction)
                .collect(Collectors.toList());
    }
}
