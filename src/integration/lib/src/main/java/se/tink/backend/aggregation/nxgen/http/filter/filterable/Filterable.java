package se.tink.backend.aggregation.nxgen.http.filter.filterable;

import se.tink.backend.aggregation.nxgen.http.filter.Filter;

public interface Filterable<F extends Filterable> {

    void cutFilterTail();

    F addFilter(Filter filter);

    F removeFilter(Filter filter);

    boolean isFilterPresent(Filter filter);
}
