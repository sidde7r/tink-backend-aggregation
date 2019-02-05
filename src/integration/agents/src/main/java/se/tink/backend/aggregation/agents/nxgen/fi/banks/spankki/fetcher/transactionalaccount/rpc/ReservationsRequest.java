package se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.fetcher.transactionalaccount.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.fetcher.transactionalaccount.entities.ReservationEntity;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.rpc.SpankkiRequest;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ReservationsRequest extends SpankkiRequest {
    private ReservationEntity reservation;

    @JsonIgnore
    public ReservationsRequest setReservationAccountId(String accountId) {
        this.reservation = new ReservationEntity().setAccountId(accountId);
        return this;
    }
    public ReservationsRequest setReservation(ReservationEntity reservation) {
        this.reservation = reservation;
        return this;
    }
}
