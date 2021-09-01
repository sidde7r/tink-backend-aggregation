package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.executor.payment.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.executor.payment.entities.Authentication;
import se.tink.backend.aggregation.annotations.JsonObject;

@AllArgsConstructor
@NoArgsConstructor
@JsonObject
public class CreateSingleScaPaymentRequest {
    private CreatePaymentRequest payment;
    private Authentication authentication;

    @JsonProperty("external_id")
    private String externalId;

    private CreateSingleScaPaymentRequest(CreateSingleScaPaymentRequest.Builder builder) {
        this.payment = builder.payment;
        this.authentication = new Authentication(builder.redirectUri, builder.state);
        this.externalId = builder.externalId;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private CreatePaymentRequest payment;
        private String redirectUri;
        private String state;
        private String externalId;

        public CreateSingleScaPaymentRequest.Builder withPayment(CreatePaymentRequest payment) {
            this.payment = payment;
            return this;
        }

        public CreateSingleScaPaymentRequest.Builder withRedirectUri(String redirectUri) {
            this.redirectUri = redirectUri;
            return this;
        }

        public CreateSingleScaPaymentRequest.Builder withState(String state) {
            this.state = state;
            return this;
        }

        public CreateSingleScaPaymentRequest.Builder withExternalId(String externalId) {
            this.externalId = externalId;
            return this;
        }

        public CreateSingleScaPaymentRequest build() {
            return new CreateSingleScaPaymentRequest(this);
        }
    }
}
