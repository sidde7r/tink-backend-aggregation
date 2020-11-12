package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.pis.rpc.domestic;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.pis.entity.domestic.DomesticPaymentConsentResponseData;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.pis.entity.domestic.Links;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.pis.entity.domestic.Meta;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.pis.entity.domestic.Risk;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;

@JsonObject
@JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy.class)
public class DomesticPaymentConsentResponse {
    private Meta meta;
    private Risk risk;
    private Links links;
    private DomesticPaymentConsentResponseData data;

    public DomesticPaymentConsentResponseData getData() {
        return data;
    }

    public PaymentResponse toTinkPaymentResponse() {
        return data.toTinkPaymentResponse();
    }

    @JsonIgnore
    public boolean hasStatusAwaitingAuthorisation() {
        return data.hasStatusAwaitingAuthorisation();
    }
}
