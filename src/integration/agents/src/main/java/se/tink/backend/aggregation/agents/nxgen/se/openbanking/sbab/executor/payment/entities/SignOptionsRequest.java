package se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab.executor.payment.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class SignOptionsRequest {

    @JsonProperty("sign_method")
    private String signMethod;

    @JsonProperty("return_url")
    private String returnUrl;

    public SignOptionsRequest(String signMethod, String returnUrl) {
        this.signMethod = signMethod;
        this.returnUrl = returnUrl;
    }

    public SignOptionsRequest() {}
}
