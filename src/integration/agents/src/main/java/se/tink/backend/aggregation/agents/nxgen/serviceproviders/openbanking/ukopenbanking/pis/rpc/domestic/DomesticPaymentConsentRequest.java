package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.rpc.domestic;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.entity.domestic.DomesticPaymentConsentRequestData;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.entity.domestic.Risk;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.payment.rpc.Payment;

@JsonObject
@JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy.class)
public class DomesticPaymentConsentRequest {
    private Risk risk;
    private DomesticPaymentConsentRequestData data;

    // Used in serialization unit tests
    protected DomesticPaymentConsentRequest() {}

    public DomesticPaymentConsentRequest(Payment payment) {
        risk = new Risk();
        data = new DomesticPaymentConsentRequestData(payment);
    }

    public DomesticPaymentConsentRequestData getData() {
        return this.data;
    }
}
