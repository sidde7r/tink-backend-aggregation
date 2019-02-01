package se.tink.backend.aggregation.agents.nxgen.es.banks.ing.fetcher.rpc;

import java.util.Collections;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public final class Movements {

    private List<Element> elements = null;
    private int limit;
    private int offset;
    private int count;
    private int total;

    public List<Element> getElements() {
        return elements == null ? Collections.emptyList() : elements;
    }

    public void setElements(List<Element> elements) {
        this.elements = elements;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }
}
