package se.tink.backend.aggregation.agents.nxgen.se.banks.volvofinans.fetcher.creditcards.rpc;

import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.se.banks.volvofinans.fetcher.creditcards.entities.CreditCardEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CreditCardsResponse {
    private List<CreditCardEntity> accounts;

    public List<CreditCardEntity> getAccounts() {
        return accounts;
    }
}
