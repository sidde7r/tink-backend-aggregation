package se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.AgentParsingUtils;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.BanquePopulaireConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class AmountEntity {
    @JsonProperty("valeur")
    private String value;

    @JsonProperty("devise")
    private String currency;

    public ExactCurrencyAmount toTinkAmount() {
        return ExactCurrencyAmount.of(
                AgentParsingUtils.parseAmount(value),
                BanquePopulaireConstants.Currency.toTinkCurrency(currency));
    }
}
