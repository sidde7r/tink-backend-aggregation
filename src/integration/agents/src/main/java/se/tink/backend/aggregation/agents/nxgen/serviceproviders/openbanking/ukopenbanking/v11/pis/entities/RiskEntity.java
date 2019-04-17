package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v11.pis.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.api.UkOpenBankingApiDefinitions;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class RiskEntity {
    @JsonProperty("PaymentContextCode")
    private UkOpenBankingApiDefinitions.ExternalPaymentContext1Code paymentContextCode;

    private RiskEntity(
            @JsonProperty("PaymentContextCode")
                    UkOpenBankingApiDefinitions.ExternalPaymentContext1Code paymentContextCode) {
        this.paymentContextCode = paymentContextCode;
    }

    @JsonIgnore
    public static RiskEntity createPersonToPerson() {
        return new RiskEntity(
                UkOpenBankingApiDefinitions.ExternalPaymentContext1Code.PERSON_TO_PERSON);
    }
}
