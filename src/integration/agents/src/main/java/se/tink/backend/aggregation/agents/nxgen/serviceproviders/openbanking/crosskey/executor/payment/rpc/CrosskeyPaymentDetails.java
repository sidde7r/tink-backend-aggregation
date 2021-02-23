package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.executor.payment.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.PropertyNamingStrategy.UpperCamelCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.authenticator.entities.accessconsents.RiskEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.executor.payment.entity.DataEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.executor.payment.enums.CrosskeyPaymentStatus;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.libraries.payment.enums.PaymentStatus;
import se.tink.libraries.payment.enums.PaymentType;
import se.tink.libraries.payment.rpc.Payment;

@JsonObject
@JsonInclude(Include.NON_NULL)
@JsonNaming(UpperCamelCaseStrategy.class)
public class CrosskeyPaymentDetails {

    private DataEntity data;

    private RiskEntity risk;

    public CrosskeyPaymentDetails() {}

    public CrosskeyPaymentDetails(Builder builder) {
        this.data = builder.data;
        this.risk = builder.risk;
    }

    @JsonIgnore
    public static CrosskeyPaymentDetails of(PaymentRequest paymentRequest) {
        RiskEntity risk = new RiskEntity();
        DataEntity data = DataEntity.of(paymentRequest);

        return new Builder().withRisk(risk).withData(data).build();
    }

    public DataEntity getData() {
        return data;
    }

    @JsonIgnore
    public PaymentResponse toTinkPayment(PaymentRequest paymentRequest) {
        Payment.Builder builder = new Payment.Builder();

        PaymentStatus paymentStatus =
                CrosskeyPaymentStatus.mapToTinkPaymentStatus(
                        CrosskeyPaymentStatus.ACCEPTED_SETTLEMENT_COMPLETED);

        builder.withCurrency(paymentRequest.getPayment().getCurrency())
                .withStatus(paymentStatus)
                .withType(PaymentType.INTERNATIONAL)
                .withExactCurrencyAmount(paymentRequest.getPayment().getExactCurrencyAmount())
                .withUniqueId(data.getConsentId())
                .withDebtor(paymentRequest.getPayment().getDebtor())
                .withCreditor(paymentRequest.getPayment().getCreditor());
        return new PaymentResponse(builder.build());
    }

    public static class Builder {
        private DataEntity data;
        private RiskEntity risk;

        public Builder withData(DataEntity data) {
            this.data = data;
            return this;
        }

        public Builder withRisk(RiskEntity risk) {
            this.risk = risk;
            return this;
        }

        public CrosskeyPaymentDetails build() {
            return new CrosskeyPaymentDetails(this);
        }
    }
}
