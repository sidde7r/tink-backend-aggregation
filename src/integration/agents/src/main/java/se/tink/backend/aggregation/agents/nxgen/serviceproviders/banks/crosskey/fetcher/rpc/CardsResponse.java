package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.fetcher.rpc;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.fetcher.entities.CrossKeyCard;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.rpc.CrossKeyResponse;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CardsResponse extends CrossKeyResponse {

    private List<CrossKeyCard> cards;
    private List<Object> data;

    public List<CrossKeyCard> getCards() {
        return Optional.ofNullable(cards).orElseGet(Collections::emptyList);
    }

    public boolean hasData() {
        return data != null && data.size() > 0;
    }
}
