package se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.fetcher.transactionalaccounts.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.BanquePopulaireConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.Amount;

@JsonObject
public class MontantEntity {
    @JsonProperty("devise")
    private String currency;
    @JsonProperty("valeur")
    private Double value;

    public Amount toTinkAmount() {
        return new Amount(BanquePopulaireConstants.Currency.toTinkCurrency(currency), value);
    }
}
