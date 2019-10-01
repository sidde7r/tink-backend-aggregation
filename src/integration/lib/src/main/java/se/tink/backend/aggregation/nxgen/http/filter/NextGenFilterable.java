package se.tink.backend.aggregation.nxgen.http.filter;

import com.google.common.base.Preconditions;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.collections4.CollectionUtils;
import se.tink.backend.aggregation.nxgen.http.filter.engine.DefaultOrderedFiltersSorter;
import se.tink.backend.aggregation.nxgen.http.filter.engine.OrderedFiltersSorter;

public abstract class NextGenFilterable<T extends Filterable> implements Filterable<T> {

    private List<Filter> filters = new ArrayList<>();
    private OrderedFiltersSorter orderedFiltersSorter = DefaultOrderedFiltersSorter.getInstance();

    public NextGenFilterable() {}

    public NextGenFilterable(List<Filter> filters) {
        this.filters = filters;
    }

    public List<Filter> getFilters() {
        return filters;
    }

    public void setOrderedFiltersSorter(OrderedFiltersSorter orderedFiltersSorter) {
        Preconditions.checkNotNull(orderedFiltersSorter);
        this.orderedFiltersSorter = orderedFiltersSorter;
    }

    public Filter getFilterHead() {
        if (CollectionUtils.isNotEmpty(filters)) {
            return filters.get(0);
        }
        return null;
    }

    @Override
    public T addFilter(Filter filter) {
        Preconditions.checkNotNull(filter);
        this.filters.add(filter);
        return (T) this;
    }

    @Override
    public T removeFilter(Filter filter) {
        filters.remove(filter);
        return (T) this;
    }

    @Override
    public boolean isFilterPresent(Filter filter) {
        return filters.contains(filter);
    }

    /** This method is kept only to keep consistent API. */
    @Override
    @Deprecated
    public void cutFilterTail() {
        throw new UnsupportedOperationException("No longer supported operation");
    }

    protected void reorderFilters() {
        filters = orderedFiltersSorter.orderFilters(filters);
        Iterator<Filter> it = filters.iterator();
        Filter next = null;
        Filter current = null;
        while (it.hasNext()) {
            next = it.next();
            if (current != null) {
                current.setNext(next);
            }
            current = next;
        }
    }
}
