package se.tink.backend.aggregation.agents.nxgen.pt.banks.caixa.fetcher.rpc;

import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.caixa.fetcher.entities.QuoteEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class MarketDetailsResponse {

    private List<QuoteEntity> quotes;

    public List<QuoteEntity> getQuotes() {
        return quotes;
    }
}
