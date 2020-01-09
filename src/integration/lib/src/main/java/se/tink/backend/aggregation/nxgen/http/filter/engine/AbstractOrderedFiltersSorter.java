package se.tink.backend.aggregation.nxgen.http.filter.engine;

import com.google.common.collect.ImmutableList;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;

public abstract class AbstractOrderedFiltersSorter
        implements OrderedFiltersSorter, Comparator<Pair<Integer, Filter>> {

    private final ImmutableList<FilterPhases> filtersCategoryOrder;

    public AbstractOrderedFiltersSorter(ImmutableList<FilterPhases> orderedList) {
        this.filtersCategoryOrder = orderedList;
    }

    @Override
    public List<Filter> orderFilters(List<Filter> unsorted) {
        LinkedHashMap<FilterPhases, List<Pair<Integer, Filter>>> result = new LinkedHashMap<>();
        for (Filter filter : unsorted) {
            FilterOrder fo = getOrderOfFilter(filter);
            if (fo != null) {
                appendTo(result, fo.category(), fo.order(), filter);
            } else {
                appendTo(result, FilterPhases.CUSTOM, 0, filter);
            }
        }

        return combineResults(result);
    }

    private List<Filter> combineResults(
            LinkedHashMap<FilterPhases, List<Pair<Integer, Filter>>> result) {
        List<Filter> sorted = new ArrayList<>();
        filtersCategoryOrder.stream()
                .forEach(
                        key -> {
                            if (result.containsKey(key)) {
                                sorted.addAll(sortFiltersCategory(result.get(key)));
                            }
                        });
        return sorted;
    }

    private List<Filter> sortFiltersCategory(List<Pair<Integer, Filter>> pairs) {
        return pairs.stream().sorted(this).map(e -> e.getRight()).collect(Collectors.toList());
    }

    @Override
    public int compare(Pair<Integer, Filter> o1, Pair<Integer, Filter> o2) {
        return o1.getLeft().compareTo(o2.getLeft());
    }

    private void appendTo(
            LinkedHashMap<FilterPhases, List<Pair<Integer, Filter>>> result,
            FilterPhases category,
            int order,
            Filter filter) {
        if (!result.containsKey(category)) {
            result.put(category, new ArrayList<>());
        }
        result.get(category).add(new ImmutablePair<>(order, filter));
    }

    public FilterOrder getOrderOfFilter(Filter filter) {
        Class fClass = filter.getClass();
        Annotation[] annotations = fClass.getAnnotations();
        FilterOrder order = null;

        for (Annotation annotation : annotations) {
            if (annotation instanceof FilterOrder) {
                order = (FilterOrder) annotation;
                break;
            }
        }

        return order;
    }
}
