package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.rpc;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.entity.DomesticScheduledPaymentConsentRequestData;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.entity.Risk;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.payment.rpc.Payment;

@JsonObject
@JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy.class)
@Data
public class DomesticScheduledPaymentConsentRequest {

    private Risk risk = new Risk();

    private DomesticScheduledPaymentConsentRequestData data;

    public DomesticScheduledPaymentConsentRequest(Payment payment) {
        this.data = new DomesticScheduledPaymentConsentRequestData(payment);
    }
}
