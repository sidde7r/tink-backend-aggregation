package se.tink.backend.aggregation.agents.nxgen.pt.banks.caixa.fetcher.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.caixa.fetcher.entities.CardAccountEntity;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.caixa.fetcher.entities.CreditBalancesEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CardAccountResponse {

    @JsonProperty("defaultCardAccount")
    private CardAccountEntity account;

    @JsonProperty("defaultCardAccountBalances")
    private CreditBalancesEntity accountBalances;

    public CardAccountEntity getAccount() {
        return account;
    }

    public CreditBalancesEntity getAccountBalances() {
        return accountBalances;
    }
}
