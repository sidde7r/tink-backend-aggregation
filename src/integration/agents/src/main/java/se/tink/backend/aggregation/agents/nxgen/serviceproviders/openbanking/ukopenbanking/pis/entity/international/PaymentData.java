package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.entity.international;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.payment.rpc.Payment;

@JsonObject
@JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy.class)
public class PaymentData {
    private String consentId;
    private PaymentInitiation initiation;

    public PaymentData() {}

    public PaymentData(
            String consentId,
            Payment payment,
            String endToEndIdentification,
            String instructionIdentification) {
        this.consentId = consentId;
        this.initiation =
                new PaymentInitiation(payment, endToEndIdentification, instructionIdentification);
    }
}
