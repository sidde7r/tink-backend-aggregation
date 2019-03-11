package se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class SignatureDataEntity {
    @JsonProperty("referenciaOTP")
    private String referenceotp;

    private String otp;

    @JsonProperty("firma")
    private String signature;

    @JsonIgnore
    private SignatureDataEntity(Builder builder) {
        referenceotp = builder.referenceotp;
        otp = builder.otp;
        signature = builder.signature;
    }

    public static class Builder {
        private String referenceotp;
        private String otp;
        private String signature;

        public SignatureDataEntity.Builder withReferenceOtp(String referenceotp) {
            this.referenceotp = referenceotp;
            return this;
        }

        public SignatureDataEntity.Builder withOtp(String otp) {
            this.otp = otp;
            return this;
        }

        public SignatureDataEntity.Builder withSignature(String signature) {
            this.signature = signature;
            return this;
        }

        public SignatureDataEntity build() {
            return new SignatureDataEntity(this);
        }
    }
}
