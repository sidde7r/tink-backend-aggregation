package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.rpc.international;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.entity.international.InternationalPaymentConsentData;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.entity.international.Risk;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.payment.rpc.Payment;

@JsonObject
@JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy.class)
public class InternationalPaymentConsentRequest {
    private Risk risk;
    private InternationalPaymentConsentData data;

    // Used in serialization unit tests
    protected InternationalPaymentConsentRequest() {}

    public InternationalPaymentConsentRequest(Payment payment) {
        this.risk = new Risk();
        this.data = new InternationalPaymentConsentData(payment);
    }
}
