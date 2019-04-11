package se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.fetcher.rpc;

import java.util.Collections;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.fetcher.entity.Element;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public final class MovementsResponse {

    private List<Element> elements = null;
    private int limit;
    private int offset;
    private int count;
    private int total;

    public List<Element> getElements() {
        return elements == null ? Collections.emptyList() : elements;
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
