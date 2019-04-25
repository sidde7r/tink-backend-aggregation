package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.pis.rpc.domestic;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import org.hibernate.validator.constraints.NotEmpty;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.pis.entity.domestic.DomesticPaymentRequestData;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.pis.entity.domestic.Risk;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.payment.rpc.Payment;

@JsonObject
@JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy.class)
public class DomesticPaymentRequest {
    private Risk risk;
    private DomesticPaymentRequestData data;

    public DomesticPaymentRequest() {}

    public DomesticPaymentRequest(
            Payment payment,
            String consentId,
            @NotEmpty String endToEndIdentification,
            @NotEmpty String instructionIdentification) {
        this.risk = new Risk();
        this.data =
                new DomesticPaymentRequestData(
                        payment, consentId,
                        endToEndIdentification, instructionIdentification);
    }
}
