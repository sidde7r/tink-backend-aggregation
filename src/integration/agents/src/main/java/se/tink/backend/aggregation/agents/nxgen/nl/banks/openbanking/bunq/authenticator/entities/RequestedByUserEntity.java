package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.bunq.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class RequestedByUserEntity {
    @JsonProperty("UserPaymentServiceProvider")
    private UserRequesterGranter userPaymentServiceProvider;
}
