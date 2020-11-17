package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.entity.domestic;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.payment.rpc.Payment;

@JsonObject
@JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy.class)
@Data
public class DomesticScheduledPaymentRequestData {

    private String consentId;

    private DomesticScheduledPaymentInitiation initiation;

    public DomesticScheduledPaymentRequestData(
            Payment payment, String consentId, String instructionIdentification) {
        this.consentId = consentId;
        this.initiation =
                new DomesticScheduledPaymentInitiation(payment, instructionIdentification);
    }
}
