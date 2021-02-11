package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.fetcher.creditcards.rpc;

import java.util.Collections;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.fetcher.creditcards.entity.CreditCardAccountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CreditCardAccountsListResponse {
    private List<CreditCardAccountEntity> creditCards;

    public List<CreditCardAccountEntity> getCreditCards() {
        return creditCards == null ? Collections.emptyList() : creditCards;
    }
}
