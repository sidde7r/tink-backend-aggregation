package se.tink.backend.aggregation.nxgen.http.filter.engine;

import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;

public class AbstractOrderedFiltersSorterTest extends AbstractSorterTest {

    @Test
    public void testCustomOrder() {
        ImmutableList<FilterPhases> phases =
                ImmutableList.of(
                        FilterPhases.CUSTOM,
                        FilterPhases.PRE_SECURITY,
                        FilterPhases.SECURITY,
                        FilterPhases.POST_SECURITY,
                        FilterPhases.PRE_PROCESS,
                        FilterPhases.SEND);
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

        List<Filter> filters = new ArrayList<>();
        filters.add(new SecurityFilter1());
        filters.add(new PostSecurityFilter2());
        filters.add(new SendFilter2());
        filters.add(new PreProcessFilter1());
        filters.add(new CustomFilter1());
        filters.add(new SecurityFilter2());
        filters.add(new PostSecurityFilter1());
        filters.add(new CustomFilter2());
        filters.add(new PreSecurityFilter2());
        filters.add(new PreProcessFilter2());
        filters.add(new SendFilter1());
        filters.add(new PreSecurityFilter1());

        CustomOrderedFiltersSorter sorter = new CustomOrderedFiltersSorter(phases);

        checkOrder(sorter.orderFilters(filters), expected);
    }

    private class CustomOrderedFiltersSorter extends AbstractOrderedFiltersSorter {

        public CustomOrderedFiltersSorter(ImmutableList<FilterPhases> orderedList) {
            super(orderedList);
        }
    }
}
