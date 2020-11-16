package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.pis.entity.domestic;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.payment.rpc.Payment;

@JsonObject
@JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy.class)
public class DomesticPaymentConsentRequestData {
    private Initiation initiation;

    // Used in serialization unit tests
    protected DomesticPaymentConsentRequestData() {}

    public DomesticPaymentConsentRequestData(Payment payment) {
        this.initiation = new Initiation(payment);
    }

    public Initiation getInitiation() {
        return this.initiation;
    }
}
