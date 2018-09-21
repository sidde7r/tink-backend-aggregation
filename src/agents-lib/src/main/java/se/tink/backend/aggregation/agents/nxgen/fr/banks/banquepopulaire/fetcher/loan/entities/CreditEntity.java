package se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.fetcher.loan.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CreditEntity {
    @JsonProperty("conditionsCredit")
    private CreditConditionsEntity creditConditions;
    @JsonProperty("conditionsAmortissement")
    private AmortizationConditionsEntity amortizationsConditions;

    public CreditConditionsEntity getCreditConditions() {
        return creditConditions;
    }

    public AmortizationConditionsEntity getAmortizationsConditions() {
        return amortizationsConditions;
    }
}
