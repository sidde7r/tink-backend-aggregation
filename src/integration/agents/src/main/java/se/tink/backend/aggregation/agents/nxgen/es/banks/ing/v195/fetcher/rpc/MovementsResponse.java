package se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.fetcher.rpc;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.fetcher.entity.IngElement;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public final class MovementsResponse {

    private List<IngElement> elements;
    private int limit;
    private int offset;
    private int count;
    private int total;

    public List<IngElement> getElements() {
        return Optional.ofNullable(elements).orElse(Collections.emptyList());
    }

    public int getLimit() {
        return limit;
    }

    public int getOffset() {
        return offset;
    }

    public int getCount() {
        return count;
    }

    public int getTotal() {
        return total;
    }
}
