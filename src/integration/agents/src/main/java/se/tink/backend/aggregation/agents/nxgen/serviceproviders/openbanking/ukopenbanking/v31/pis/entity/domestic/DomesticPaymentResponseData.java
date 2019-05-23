package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.pis.entity.domestic;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.payment.rpc.Payment;

@JsonObject
@JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy.class)
public class DomesticPaymentResponseData {
    private String domesticPaymentId;
    private String consentId;
    private DomesticPaymentResponseInitiation initiation;

    public Payment toTinkPayment() {
        return initiation.toTinkPayment(consentId, domesticPaymentId);
    }
}
