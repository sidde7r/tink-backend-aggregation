package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.executors.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CompleteTransferErrorEntity {
    @JsonProperty("payment_id")
    private String paymentId;
}
