package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.entity.domestic;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;

@JsonObject
@JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy.class)
public class DomesticPaymentResponseData {
    private String domesticPaymentId;
    private String consentId;
    private DomesticPaymentResponseInitiation initiation;
    private String status;

    public PaymentResponse toTinkPaymentResponse() {
        return initiation.toTinkPaymentResponse(consentId, domesticPaymentId, status);
    }
}
