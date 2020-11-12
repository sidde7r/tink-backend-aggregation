package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.pis.entity.domestic;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.payment.rpc.Payment;

@JsonObject
@JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy.class)
@Getter
public class DomesticPaymentRequestData {
    private String consentId;
    private DomesticPaymentInitiation initiation;

    // Used in serialization unit tests
    protected DomesticPaymentRequestData() {}

    public DomesticPaymentRequestData(
            Payment payment,
            String consentId,
            String endToEndIdentification,
            String instructionIdentification) {
        this.consentId = consentId;
        this.initiation =
                new DomesticPaymentInitiation(
                        payment, endToEndIdentification, instructionIdentification);
    }
}
