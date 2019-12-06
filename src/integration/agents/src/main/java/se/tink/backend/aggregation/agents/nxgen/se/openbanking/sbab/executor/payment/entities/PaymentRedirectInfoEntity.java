package se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab.executor.payment.entities;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class PaymentRedirectInfoEntity {

    private String state;
    private SignOptionsResponse signOptionsResponse;

    @JsonCreator
    public PaymentRedirectInfoEntity(
            @JsonProperty("state") String state,
            @JsonProperty("signOptionsResponse") SignOptionsResponse signOptionsResponse) {
        this.state = state;
        this.signOptionsResponse = signOptionsResponse;
    }

    public String getState() {
        return state;
    }

    public SignOptionsResponse getSignOptionsResponse() {
        return signOptionsResponse;
    }
}
