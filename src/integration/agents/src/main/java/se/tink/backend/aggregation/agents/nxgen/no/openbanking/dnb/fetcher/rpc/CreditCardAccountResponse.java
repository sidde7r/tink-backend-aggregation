package se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.fetcher.rpc;

import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.fetcher.entity.CardAccountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CreditCardAccountResponse {

    private List<CardAccountEntity> cardAccounts;

    public List<CardAccountEntity> getAccount() {
        return cardAccounts;
    }
}
