package se.tink.backend.common.search.parsers;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.assertj.core.util.Lists;
import org.elasticsearch.index.query.FilterBuilder;
import org.junit.Test;
import se.tink.libraries.i18n.Catalog;
import se.tink.backend.common.search.SearchParserContext;
import se.tink.backend.core.User;
import se.tink.backend.core.UserProfile;
import se.tink.libraries.date.ResolutionTypes;
import static org.assertj.core.api.Assertions.assertThat;

public class TimeSpanParserCommandTest {
    private static final TypeReference<List<Filter>> LIST_TYPE_REFERENCE = new TypeReference<List<Filter>>() {
    };

    @Test
    public void createYearlyFilter() throws IOException {
        TimeSpanParserCommand command = new TimeSpanParserCommand();
        SearchParserContext context = getDefaultSearchParserContext();

        command.parse(Lists.newArrayList("2016"), context, true);
        assertThat(context.getQueryFilters()).hasSize(1);
        Set<FilterSpan> filterSpans = toFilterSpanSet(context.getQueryFilters());
        assertThat(filterSpans).hasSameSizeAs(context.getQueryFilters());
        assertThat(filterSpans).containsOnly(new FilterSpan(20160101, 20161231));
    }

    @Test
    public void createMonthlyOldYearFilter() throws IOException {
        TimeSpanParserCommand command = new TimeSpanParserCommand();
        SearchParserContext context = getDefaultSearchParserContext();

        command.parse(Lists.newArrayList("juni", "2014"), context, true);
        assertThat(context.getQueryFilters()).hasSize(1);
        Set<FilterSpan> filterSpans = toFilterSpanSet(context.getQueryFilters());
        assertThat(filterSpans).hasSameSizeAs(context.getQueryFilters());
        assertThat(filterSpans).containsOnly(new FilterSpan(20140523, 20140624));
    }

    @Test
    public void createMonthlyWeekendFilter() throws IOException {
        TimeSpanParserCommand command = new TimeSpanParserCommand();
        SearchParserContext context = getDefaultSearchParserContext();

        command.parse(Lists.newArrayList("juni", "2014", "helger"), context, true);
        assertThat(context.getQueryFilters()).hasSize(2);
        Set<FilterSpan> filterSpans = toFilterSpanSet(context.getQueryFilters());
        assertThat(filterSpans).hasSameSizeAs(context.getQueryFilters());
        assertThat(filterSpans).containsOnly(new FilterSpan(20140523, 20140624), new FilterSpan(6, 7));
    }

    @Test
    public void createMonthlyDefaultFilter() throws IOException {
        TimeSpanParserCommand command = new TimeSpanParserCommand();
        SearchParserContext context1 = getDefaultSearchParserContext();
        SearchParserContext context2 = getDefaultSearchParserContext();

        command.parse(Lists.newArrayList("januari"), context1, true);
        command.parse(Lists.newArrayList("januari", "i", "år"), context2, true);
        assertThat(context1.getQueryFilters()).hasSize(1);
        assertThat(context2.getQueryFilters()).hasSize(1);
        assertThat(toFilterSpanSet(context2.getQueryFilters()))
                .containsOnlyElementsOf(toFilterSpanSet(context1.getQueryFilters()));
    }

    @Test
    public void createMonthlyLastYearFilter() throws IOException {
        TimeSpanParserCommand command = new TimeSpanParserCommand();
        SearchParserContext context1 = getDefaultSearchParserContext();
        SearchParserContext context2 = getDefaultSearchParserContext();
        SearchParserContext context3 = getDefaultSearchParserContext();
        Calendar now = Calendar.getInstance();
        Integer lastYear = now.get(Calendar.YEAR) - 1;

        command.parse(Lists.newArrayList("januari", lastYear.toString()), context1, true);
        command.parse(Lists.newArrayList("januari", "förra", "året"), context2, true);
        command.parse(Lists.newArrayList("januari"), context3, true);
        assertThat(context1.getQueryFilters()).hasSize(1);
        assertThat(context2.getQueryFilters()).hasSize(1);
        assertThat(context3.getQueryFilters()).hasSize(1);

        Set<FilterSpan> filterSpansLastYear1 = toFilterSpanSet(context1.getQueryFilters());
        Set<FilterSpan> filterSpansLastYear2 = toFilterSpanSet(context2.getQueryFilters());
        Set<FilterSpan> filterSpansThisYear = toFilterSpanSet(context3.getQueryFilters());

        assertThat(filterSpansLastYear2).containsOnlyElementsOf(filterSpansLastYear1);
        assertThat(filterSpansLastYear2).doesNotContainAnyElementsOf(filterSpansThisYear);
    }

    @SuppressWarnings("unchecked")
    private Set<FilterSpan> toFilterSpanSet(List<FilterBuilder> filterBuilders) throws IOException {
        return ((List<Filter>) new ObjectMapper().readValue(filterBuilders.toString(), LIST_TYPE_REFERENCE)).stream()
                .map(Filter::getRange)
                .flatMap(range -> Lists.newArrayList(range.getDate(), range.getDayOfWeek()).stream())
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    private SearchParserContext getDefaultSearchParserContext() {
        SearchParserContext context = new SearchParserContext();
        context.setLocale(Catalog.getLocale("sv_SE"));
        context.setCatalog(Catalog.getCatalog("sv_SE"));
        context.setQueryFilters(Lists.newArrayList());

        UserProfile userProfile = new UserProfile();
        userProfile.setPeriodMode(ResolutionTypes.MONTHLY_ADJUSTED);
        userProfile.setPeriodAdjustedDay(25);
        User user = new User();
        user.setProfile(userProfile);

        context.setUser(user);
        return context;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class FilterSpan {
        private int from;
        private int to;

        public FilterSpan() {
        }

        FilterSpan(int from, int to) {
            this.from = from;
            this.to = to;
        }

        public int getFrom() {
            return from;
        }

        public int getTo() {
            return to;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            FilterSpan that = (FilterSpan) o;

            return from == that.from && to == that.to;
        }

        @Override
        public int hashCode() {
            int result = from;
            result = 31 * result + to;
            return result;
        }

        @Override
        public String toString() {
            return "FilterSpan{" +
                    "from=" + from +
                    ", to=" + to +
                    '}';
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class FilterRange {
        private FilterSpan dayOfWeek;
        private FilterSpan date;

        public FilterSpan getDayOfWeek() {
            return dayOfWeek;
        }

        public FilterSpan getDate() {
            return date;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class Filter {
        private FilterRange range;

        public FilterRange getRange() {
            return range;
        }
    }
}
