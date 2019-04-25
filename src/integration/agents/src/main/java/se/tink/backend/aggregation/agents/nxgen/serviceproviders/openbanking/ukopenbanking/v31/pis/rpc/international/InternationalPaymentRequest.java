package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.pis.rpc.international;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.pis.entity.international.PaymentData;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.pis.entity.international.RiskExtended;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.payment.rpc.Payment;

@JsonObject
@JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy.class)
public class InternationalPaymentRequest {
    private RiskExtended risk;
    private PaymentData data;

    public InternationalPaymentRequest() {}

    public InternationalPaymentRequest(
            Payment payment,
            String consentId,
            String endToEndIdentification,
            String instructionIdentification) {
        this.data =
                new PaymentData(
                        consentId, payment, endToEndIdentification, instructionIdentification);
        this.risk = new RiskExtended();
    }
}
