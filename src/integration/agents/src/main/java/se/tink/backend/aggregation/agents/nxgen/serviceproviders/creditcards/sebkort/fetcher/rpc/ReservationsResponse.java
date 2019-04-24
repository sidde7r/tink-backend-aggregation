package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.sebkort.fetcher.rpc;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.sebkort.fetcher.entity.TransactionEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ReservationsResponse {
    private boolean moreDataExists;
    private List<TransactionEntity> reservations;

    public boolean moreDataExists() {
        return moreDataExists;
    }

    public List<TransactionEntity> getReservations() {
        return Optional.ofNullable(reservations).orElseGet(Collections::emptyList);
    }
}
