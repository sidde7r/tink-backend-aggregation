package se.tink.backend.aggregation.nxgen.http.filter.engine;

import java.util.List;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;

public interface OrderedFiltersSorter {

    /**
     * Creates new sorted list which contains sorted elements of unsorted list.
     *
     * @param unsorted - list of elements
     * @return new - sorted list
     */
    List<Filter> orderFilters(List<Filter> unsorted);
}
