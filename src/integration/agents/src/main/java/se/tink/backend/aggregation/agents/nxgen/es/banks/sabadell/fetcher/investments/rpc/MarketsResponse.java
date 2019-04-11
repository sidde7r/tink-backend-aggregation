package se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.investments.rpc;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.investments.entities.MarketsEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class MarketsResponse {
    private List<MarketsEntity> markets;

    public List<MarketsEntity> getMarkets() {
        return Optional.ofNullable(markets).orElse(Collections.emptyList());
    }
}
