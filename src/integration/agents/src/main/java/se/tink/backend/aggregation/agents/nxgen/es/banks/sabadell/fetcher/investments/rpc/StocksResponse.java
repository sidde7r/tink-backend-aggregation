package se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.investments.rpc;

import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.investments.entities.StocksEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class StocksResponse {
    private List<StocksEntity> stocks;

    public List<StocksEntity> getStocks() {
        return stocks;
    }
}
