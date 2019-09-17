package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bec.executor.payment.entities;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class PaymentRedirectInfoEntity {

    private String state;
    private String redirectUrl;

    @JsonCreator
    public PaymentRedirectInfoEntity(
            @JsonProperty("state") String state, @JsonProperty("redirectUrl") String redirectUrl) {
        this.state = state;
        this.redirectUrl = redirectUrl;
    }

    public String getState() {
        return state;
    }

    public String getRedirectUrl() {
        return redirectUrl;
    }
}
