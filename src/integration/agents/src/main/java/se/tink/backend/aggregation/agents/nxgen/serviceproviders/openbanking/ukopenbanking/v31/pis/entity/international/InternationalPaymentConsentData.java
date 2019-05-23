package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.pis.entity.international;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.payment.rpc.Payment;

@JsonObject
@JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy.class)
public class InternationalPaymentConsentData {
    private InternationalPaymentConsentInitiationReq initiation;

    public InternationalPaymentConsentData() {}

    public InternationalPaymentConsentData(Payment payment) {
        this.initiation = new InternationalPaymentConsentInitiationReq();
    }
}
