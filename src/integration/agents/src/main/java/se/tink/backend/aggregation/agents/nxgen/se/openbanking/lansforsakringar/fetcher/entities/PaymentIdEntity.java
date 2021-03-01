package se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.fetcher.entities;

import lombok.Getter;
import lombok.Setter;
import se.tink.backend.aggregation.annotations.JsonObject;

@Setter
@Getter
@JsonObject
public class PaymentIdEntity {

    private String paymentId;
}
