package se.tink.backend.aggregation.nxgen.http.filter.engine;

import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;

public class DefaultOrderedFiltersSorterTest extends AbstractSorterTest {

    private OrderedFiltersSorter sorter = DefaultOrderedFiltersSorter.getInstance();

    private static final List<Class> EXPECTED_DEFAULT_ORDERED =
            ImmutableList.of(
                    PreProcessFilter1.class,
                    PreProcessFilter2.class,
                    PreProcessFilter3.class,
                    PreProcessFilter4.class,
                    PreProcessFilter5.class,
                    NotAnnotated1.class,
                    CustomFilter1.class,
                    CustomFilter2.class,
                    CustomFilter3.class,
                    CustomFilter4.class,
                    CustomFilter5.class,
                    PreSecurityFilter1.class,
                    PreSecurityFilter2.class,
                    PreSecurityFilter3.class,
                    PreSecurityFilter4.class,
                    PreSecurityFilter5.class,
                    SecurityFilter1.class,
                    SecurityFilter2.class,
                    SecurityFilter3.class,
                    SecurityFilter4.class,
                    SecurityFilter5.class,
                    PostSecurityFilter1.class,
                    PostSecurityFilter2.class,
                    PostSecurityFilter3.class,
                    PostSecurityFilter4.class,
                    PostSecurityFilter5.class,
                    SendFilter1.class,
                    SendFilter2.class,
                    SendFilter3.class,
                    SendFilter4.class,
                    SendFilter5.class);

    private List<Filter> prepareSchuffeledList() {
        List<Filter> filters = new ArrayList<>();
        filters.add(new PostSecurityFilter2());
        filters.add(new PreProcessFilter1());
        filters.add(new NotAnnotated1());
        filters.add(new CustomFilter2());
        filters.add(new PreSecurityFilter2());
        filters.add(new SendFilter4());
        filters.add(new CustomFilter1());
        filters.add(new PreProcessFilter2());
        filters.add(new SecurityFilter5());
        filters.add(new SendFilter3());
        filters.add(new PostSecurityFilter5());
        filters.add(new PreSecurityFilter1());
        filters.add(new PreProcessFilter3());
        filters.add(new SendFilter2());
        filters.add(new PreSecurityFilter5());
        filters.add(new SecurityFilter1());
        filters.add(new PreProcessFilter4());
        filters.add(new CustomFilter5());
        filters.add(new PreSecurityFilter4());
        filters.add(new SecurityFilter2());
        filters.add(new SecurityFilter4());
        filters.add(new PostSecurityFilter1());
        filters.add(new PreProcessFilter5());
        filters.add(new CustomFilter4());
        filters.add(new PreSecurityFilter3());
        filters.add(new PostSecurityFilter3());
        filters.add(new SecurityFilter3());
        filters.add(new SendFilter1());
        filters.add(new PostSecurityFilter4());
        filters.add(new CustomFilter3());
        filters.add(new SendFilter5());
        return filters;
    }

    @Test
    public void defaultFiltersSorting_shuffleManually_sortedList() {
        // Feature: Ordering filters list by DefaultOrderedFiltersSorter
        // Scenario: Shuffled manually filters list sorting
        // Given:
        List<Filter> filters = prepareSchuffeledList();
        // When: list gets sorted
        List<Filter> sorted = sorter.orderFilters(filters);
        // Then: - check if order is as expected
        checkOrder(sorted, EXPECTED_DEFAULT_ORDERED);
    }

    @Test
    public void defaultFiltersSorting_shuffleRandomOrder_sortedList() {
        // Feature: Ordering filters list by DefaultOrderedFiltersSorter
        // Scenario: Shuffled manually filters list sorting
        // Given:
        List<Filter> filters = prepareSchuffeledList();

        filters = createRandomOrderShuffledList(filters);
        // When: list gets sorted
        List<Filter> sorted = sorter.orderFilters(filters);
        // Then: - check if order is as expected
        checkOrder(sorted, EXPECTED_DEFAULT_ORDERED);
    }
}
