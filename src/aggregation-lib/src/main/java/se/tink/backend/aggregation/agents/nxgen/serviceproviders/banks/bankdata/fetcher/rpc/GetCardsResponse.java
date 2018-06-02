package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.fetcher.rpc;

import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.fetcher.entities.BankdataCardEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class GetCardsResponse {
    private List<BankdataCardEntity> cards;

    public List<BankdataCardEntity> getCards() {
        return cards;
    }
}
