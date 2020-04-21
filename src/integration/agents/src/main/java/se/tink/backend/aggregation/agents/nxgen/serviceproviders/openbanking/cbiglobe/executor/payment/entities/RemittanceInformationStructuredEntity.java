package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.executor.payment.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;

@JsonObject
public class RemittanceInformationStructuredEntity {
    private String reference;
    private String referenceType;
    private String referenceIssuer;

    public RemittanceInformationStructuredEntity() {}

    public RemittanceInformationStructuredEntity(
            String reference, String referenceType, String referenceIssuer) {
        this.reference = reference;
        this.referenceType = referenceType;
        this.referenceIssuer = referenceIssuer;
    }

    @JsonIgnore
    public static RemittanceInformationStructuredEntity of(PaymentRequest paymentRequest) {
        return new RemittanceInformationStructuredEntity(
                paymentRequest.getPayment().getReference().getValue(),
                paymentRequest.getPayment().getReference().getType(),
                null);
    }
}
