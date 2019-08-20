package se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v1.fetcher.transactionalaccount.rpc;

import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v1.fetcher.transactionalaccount.entities.ReservationsEntity;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v1.rpc.SpankkiResponse;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ReservationsResponse extends SpankkiResponse {
    private List<ReservationsEntity> reservations;

    public List<ReservationsEntity> getReservations() {
        return reservations;
    }
}
