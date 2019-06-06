package se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab.executor.payment.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class SignOptionsRequest {

    @JsonProperty("sign_method")
    private String signMethod;

    @JsonProperty("return_url")
    private String returnUrl;

    private SignOptionsRequest(Builder builder) {
        this.signMethod = builder.signMethod;
        this.returnUrl = builder.returnUrl;
    }

    public static class Builder {
        private String signMethod;
        private String returnUrl;

        public Builder withSignMethod(String signMethod) {
            this.signMethod = signMethod;
            return this;
        }

        public Builder withReturnUrl(String returnUrl) {
            this.returnUrl = returnUrl;
            return this;
        }

        public SignOptionsRequest build() {
            return new SignOptionsRequest(this);
        }
    }
}
