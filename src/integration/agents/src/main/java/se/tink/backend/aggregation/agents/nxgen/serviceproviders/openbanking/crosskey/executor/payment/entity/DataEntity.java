package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.executor.payment.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.PropertyNamingStrategy.UpperCamelCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.executor.payment.enums.CrosskeyPaymentStatus;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;

@JsonObject
@JsonNaming(UpperCamelCaseStrategy.class)
@JsonInclude(Include.NON_NULL)
public class DataEntity {

    private String consentId;

    private InitiationEntity initiation;

    private String status;

    private String internationalPaymentId;

    public DataEntity() {}

    @JsonIgnore
    private DataEntity(Builder builder) {
        this.consentId = builder.consentId;
        this.status = builder.status;
        this.initiation = builder.initiation;
    }

    @JsonIgnore
    public static DataEntity of(PaymentRequest paymentRequest) {
        InitiationEntity initiationEntity = InitiationEntity.of(paymentRequest);
        return new Builder()
                .withStatus(
                        CrosskeyPaymentStatus.mapToCrosskeyPaymentStatus(
                                        paymentRequest.getPayment().getStatus())
                                .name())
                .withInitiation(initiationEntity)
                .withConsentId(paymentRequest.getPayment().getUniqueId())
                .build();
    }

    public String getConsentId() {
        return consentId;
    }

    public void setConsentId(String consentId) {
        this.consentId = consentId;
    }

    public InitiationEntity getInitiation() {
        return initiation;
    }

    public void setInitiation(InitiationEntity initiation) {
        this.initiation = initiation;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getInternationalPaymentId() {
        return internationalPaymentId;
    }

    public void setInternationalPaymentId(String internationalPaymentId) {
        this.internationalPaymentId = internationalPaymentId;
    }

    public static class Builder {
        private String consentId;
        private String status;
        private InitiationEntity initiation;

        public Builder withConsentId(String consentId) {
            this.consentId = consentId;
            return this;
        }

        public Builder withStatus(String status) {
            this.status = status;
            return this;
        }

        public Builder withInitiation(InitiationEntity initiation) {
            this.initiation = initiation;
            return this;
        }

        public DataEntity build() {
            return new DataEntity(this);
        }
    }
}
