package se.tink.backend.aggregation.nxgen.http.filter.engine;

import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;

public class CustomOrderedFiltersSorterTest extends AbstractSorterTest {

    @Test
    public void orderFilters_shuffleManually_sortedList() {
        // Feature: Ordering filters list by CustomOrderedFiltersSorter - test of
        // AbstractOrderedFiltersSorter implementation
        // Scenario: Shuffled manually filters list sorting
        // Given:
        // <editor-fold defaultstate="collapsed" desc="Prepare test given values">
        // wanted phases order
        ImmutableList<FilterPhases> phases =
                ImmutableList.of(
                        FilterPhases.CUSTOM,
                        FilterPhases.PRE_SECURITY,
                        FilterPhases.SECURITY,
                        FilterPhases.POST_SECURITY,
                        FilterPhases.PRE_PROCESS,
                        FilterPhases.SEND);
        // unsorted filters list
        List<Filter> filters = new ArrayList<>();
        filters.add(new CustomFilter1());
        filters.add(new SendFilter2());
        filters.add(new SecurityFilter1());
        filters.add(new SendFilter1());
        filters.add(new PreSecurityFilter2());
        filters.add(new PostSecurityFilter2());
        filters.add(new PreSecurityFilter1());
        filters.add(new PreProcessFilter2());
        filters.add(new PreProcessFilter1());
        filters.add(new SecurityFilter2());
        filters.add(new PostSecurityFilter1());
        filters.add(new CustomFilter2());
        // wanted phases order
        ImmutableList<Class> expected =
                ImmutableList.of(
                        CustomFilter1.class,
                        CustomFilter2.class,
                        PreSecurityFilter1.class,
                        PreSecurityFilter2.class,
                        SecurityFilter1.class,
                        SecurityFilter2.class,
                        PostSecurityFilter1.class,
                        PostSecurityFilter2.class,
                        PreProcessFilter1.class,
                        PreProcessFilter2.class,
                        SendFilter1.class,
                        SendFilter2.class);

        // custom ordering implementation
        CustomOrderedFiltersSorter sorter = new CustomOrderedFiltersSorter(phases);

        // </editor-fold>

        // When: list gets sorted
        filters = sorter.orderFilters(filters);

        // Then: - check if order is as expected
        checkOrder(filters, expected);
    }

    @Test
    public void orderFilters_randomOrderShuffleManually_sortedList() {
        // Feature: Ordering filters list by CustomOrderedFiltersSorter - test of
        // AbstractOrderedFiltersSorter implementation
        // Scenario: Shuffled manually filters list sorting
        // Given:
        // <editor-fold defaultstate="collapsed" desc="Prepare test given values">
        // wanted phases order
        ImmutableList<FilterPhases> phases =
                ImmutableList.of(
                        FilterPhases.CUSTOM,
                        FilterPhases.PRE_SECURITY,
                        FilterPhases.SECURITY,
                        FilterPhases.POST_SECURITY,
                        FilterPhases.PRE_PROCESS,
                        FilterPhases.SEND);
        // unsorted filters list
        List<Filter> filters = new ArrayList<>();
        filters.add(new CustomFilter1());
        filters.add(new SendFilter2());
        filters.add(new SecurityFilter1());
        filters.add(new SendFilter1());
        filters.add(new PreSecurityFilter2());
        filters.add(new PostSecurityFilter2());
        filters.add(new PreSecurityFilter1());
        filters.add(new PreProcessFilter2());
        filters.add(new PreProcessFilter1());
        filters.add(new SecurityFilter2());
        filters.add(new PostSecurityFilter1());
        filters.add(new CustomFilter2());
        filters = createRandomOrderShuffledList(filters);
        // wanted phases order
        ImmutableList<Class> expected =
                ImmutableList.of(
                        CustomFilter1.class,
                        CustomFilter2.class,
                        PreSecurityFilter1.class,
                        PreSecurityFilter2.class,
                        SecurityFilter1.class,
                        SecurityFilter2.class,
                        PostSecurityFilter1.class,
                        PostSecurityFilter2.class,
                        PreProcessFilter1.class,
                        PreProcessFilter2.class,
                        SendFilter1.class,
                        SendFilter2.class);

        // custom ordering implementation
        CustomOrderedFiltersSorter sorter = new CustomOrderedFiltersSorter(phases);

        // </editor-fold>

        // When: list gets sorted
        filters = sorter.orderFilters(filters);

        // Then: - check if order is as expected
        checkOrder(filters, expected);
    }

    private class CustomOrderedFiltersSorter extends AbstractOrderedFiltersSorter {

        public CustomOrderedFiltersSorter(ImmutableList<FilterPhases> orderedList) {
            super(orderedList);
        }
    }
}
