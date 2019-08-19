package se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.executor.payment.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.executor.payment.entities.PsuDataEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AuthorizePaymentRequest {
    private PsuDataEntity psuData;

    @JsonIgnore
    public AuthorizePaymentRequest(PsuDataEntity psuData) {
        this.psuData = psuData;
    }
}
