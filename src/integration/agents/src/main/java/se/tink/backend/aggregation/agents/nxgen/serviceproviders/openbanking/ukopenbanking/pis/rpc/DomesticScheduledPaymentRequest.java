package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.rpc;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.entity.DomesticScheduledPaymentRequestData;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.entity.Risk;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.payment.rpc.Payment;

@JsonObject
@JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy.class)
@Data
public class DomesticScheduledPaymentRequest {

    private Risk risk = new Risk();

    private DomesticScheduledPaymentRequestData data;

    public DomesticScheduledPaymentRequest(
            Payment payment, String consentId, String instructionIdentification) {

        this.data =
                new DomesticScheduledPaymentRequestData(
                        payment, consentId, instructionIdentification);
    }
}
