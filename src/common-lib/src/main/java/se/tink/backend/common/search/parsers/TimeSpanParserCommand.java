package se.tink.backend.common.search.parsers;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;
import com.google.common.primitives.Ints;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;
import org.elasticsearch.index.query.FilterBuilders;
import org.elasticsearch.index.query.RangeFilterBuilder;
import se.tink.libraries.i18n.Catalog;
import se.tink.backend.common.search.SearchParser;
import se.tink.backend.common.search.SearchParserCommand;
import se.tink.backend.common.search.SearchParserContext;
import se.tink.libraries.date.DateUtils;
import se.tink.libraries.date.ResolutionTypes;
import se.tink.libraries.date.ThreadSafeDateFormat;

public class TimeSpanParserCommand extends SearchParserCommand {
    protected static RangeFilterBuilder createYearFilter(int year) {
        return FilterBuilders.rangeFilter("date").from(Integer.parseInt(year + "0101"))
                .to(Integer.parseInt(year + "1231"));
    }

    protected static RangeFilterBuilder createDateFilter(Date from, Date to) {
        return FilterBuilders.rangeFilter("date").from(DateUtils.toInteger(from)).to(DateUtils.toInteger(to));
    }

    protected static RangeFilterBuilder createDateFilter(Date date) {
        return createDateFilter(date, date);
    }

    protected HashMap<String, Supplier<TimeSpanFilterBuilder>> staticTimeSpanFilters = new HashMap<>();

    public TimeSpanParserCommand() {
        createStaticTimeSpanFilters();
    }

    private HashMap<String, Supplier<TimeSpanFilterBuilder>> createDynamicTimeSpanFilters(SearchParserContext context) {
        HashMap<String, Supplier<TimeSpanFilterBuilder>> dynamicTimeSpanFilters = Maps.newHashMap();

        ResolutionTypes resolution = context.getUser().getProfile().getPeriodMode();
        int periodAdjustedDate = context.getUser().getProfile().getPeriodAdjustedDay();

        // Add weekdays and weekends.

        dynamicTimeSpanFilters.put(context.getCatalog().getString("weekdays"), () ->
                new TimeSpanFilterBuilder((calendar) -> FilterBuilders.rangeFilter("dayOfWeek").from(1).to(5), null));

        dynamicTimeSpanFilters.put(context.getCatalog().getString("weekends"), () ->
                new TimeSpanFilterBuilder((calendar) -> FilterBuilders.rangeFilter("dayOfWeek").from(6).to(7), null));

        // Add today and yesterday.

        dynamicTimeSpanFilters.put(context.getCatalog().getString("today"), () -> {
            Calendar today = DateUtils.getCalendar(context.getLocale());
            return new TimeSpanFilterBuilder(calendar -> createDateFilter(calendar.getTime()),
                    today, Calendar.DAY_OF_YEAR, today.get(Calendar.DAY_OF_YEAR));
        });

        dynamicTimeSpanFilters.put(context.getCatalog().getString("yesterday"), () -> {
            Calendar yesterday = DateUtils.getCalendar(context.getLocale());
            yesterday.add(Calendar.DAY_OF_YEAR, -1);
            return new TimeSpanFilterBuilder(calendar -> createDateFilter(calendar.getTime()),
                    yesterday, Calendar.DAY_OF_YEAR, yesterday.get(Calendar.DAY_OF_YEAR));
        });

        // Add current and last week.

        dynamicTimeSpanFilters.put(context.getCatalog().getString("this week"), () -> {
            Calendar thisWeek = DateUtils.getCalendar(context.getLocale());
            return new TimeSpanFilterBuilder(this::createWeeklyFilter, thisWeek, Calendar.WEEK_OF_YEAR,
                    thisWeek.get(Calendar.WEEK_OF_YEAR));
        });

        dynamicTimeSpanFilters.put(context.getCatalog().getString("last week"), () -> {
            Calendar lastWeek = DateUtils.getCalendar(context.getLocale());
            lastWeek.add(Calendar.WEEK_OF_YEAR, -1);
            return new TimeSpanFilterBuilder(this::createWeeklyFilter, lastWeek, Calendar.WEEK_OF_YEAR,
                    lastWeek.get(Calendar.WEEK_OF_YEAR));
        });

        // Add current and last month.

        dynamicTimeSpanFilters.put(context.getCatalog().getString("this month"), () -> {
            Calendar now = DateUtils.getCalendar(context.getLocale());
            return new TimeSpanFilterBuilder(calendar -> createMonthlyFilter(calendar, resolution, periodAdjustedDate),
                    now, Calendar.MONTH, now.get(Calendar.MONTH));
        });

        dynamicTimeSpanFilters.put(context.getCatalog().getString("last month"), () -> {
            Calendar lastMonth = DateUtils.getCalendar(context.getLocale());
            String lastMonthPeriod = DateUtils
                    .getPreviousMonthPeriod(
                            DateUtils.getMonthPeriod(lastMonth.getTime(), resolution, periodAdjustedDate));
            lastMonth.setTime(DateUtils.getFirstDateFromPeriod(lastMonthPeriod));
            return new TimeSpanFilterBuilder(calendar -> createMonthlyFilter(calendar, resolution, periodAdjustedDate),
                    lastMonth, Calendar.MONTH, lastMonth.get(Calendar.MONTH));
        });

        // Add current and last year.

        dynamicTimeSpanFilters.put(context.getCatalog().getString("this year"), () -> {
            Calendar thisYear = DateUtils.getCalendar(context.getLocale());
            return new TimeSpanFilterBuilder(this::createYearlyFilter, thisYear, Calendar.YEAR,
                    thisYear.get(Calendar.YEAR));
        });

        dynamicTimeSpanFilters.put(context.getCatalog().getString("last year"), () -> {
            Calendar lastYear = DateUtils.getCalendar(context.getLocale());
            lastYear.add(Calendar.YEAR, -1);
            return new TimeSpanFilterBuilder(this::createYearlyFilter, lastYear, Calendar.YEAR,
                    lastYear.get(Calendar.YEAR));
        });

        // Add past 12 months.

        ThreadSafeDateFormat monthFormat = ThreadSafeDateFormat.FORMATTER_MONTH_NAME
                .toBuilder().setLocale(context.getLocale()).build();
        Calendar now = DateUtils.getCalendar(context.getLocale());
        String monthPeriod = DateUtils.getMonthPeriod(now.getTime(), resolution, periodAdjustedDate);

        for (int i = 0; i < 12; i++) {
            String finalMonthPeriod = monthPeriod;
            dynamicTimeSpanFilters.put(
                    monthFormat.format(DateUtils.parseDate(monthPeriod)).toLowerCase(), () -> {
                        Calendar month = DateUtils.getCalendar(context.getLocale());
                        month.setTime(DateUtils.getFirstDateFromPeriod(finalMonthPeriod));
                        return new TimeSpanFilterBuilder(
                                calendar -> createMonthlyFilter(calendar, resolution, periodAdjustedDate),
                                month, Calendar.MONTH, month.get(Calendar.MONTH));
                    });
            monthPeriod = DateUtils.getPreviousMonthPeriod(monthPeriod);
        }

        // Add weeks.

        Calendar defaultCalendar = DateUtils.getCalendar(context.getLocale());

        // Loop through the past 52 weeks.

        for (int i = 0; i < 52; i++) {
            Calendar week = (Calendar) defaultCalendar.clone();
            week.add(Calendar.WEEK_OF_YEAR, -i);
            dynamicTimeSpanFilters.put(
                    Catalog.format(context.getCatalog().getString("week {0}"), week.get(Calendar.WEEK_OF_YEAR)),
                    () -> new TimeSpanFilterBuilder(this::createWeeklyFilter, week, Calendar.WEEK_OF_YEAR,
                            week.get(Calendar.WEEK_OF_YEAR)));
        }

        return dynamicTimeSpanFilters;
    }

    private RangeFilterBuilder createYearlyFilter(Calendar year) {
        return createYearFilter(year.get(Calendar.YEAR));
    }

    private RangeFilterBuilder createMonthlyFilter(String monthPeriod, ResolutionTypes resolution,
            int periodAdjustedDate) {
        Date firsDayOfMonth = DateUtils
                .getFirstDateFromPeriod(monthPeriod, resolution, periodAdjustedDate);
        Date lastDayOfMonth = DateUtils
                .getLastDateFromPeriod(monthPeriod, resolution, periodAdjustedDate);
        return createDateFilter(firsDayOfMonth, lastDayOfMonth);
    }

    private RangeFilterBuilder createMonthlyFilter(Calendar calendar, ResolutionTypes resolution,
            int periodAdjustedDate) {
        String monthPeriod = DateUtils.getMonthPeriod(calendar.getTime(), resolution, periodAdjustedDate);
        return createMonthlyFilter(monthPeriod, resolution, periodAdjustedDate);
    }

    private RangeFilterBuilder createWeeklyFilter(Calendar week) {
        week.set(Calendar.DAY_OF_WEEK, week.getFirstDayOfWeek()); // first and lsat time
        Calendar weekEndDate = (Calendar) week.clone();
        weekEndDate.add(Calendar.DAY_OF_YEAR, 6);
        return createDateFilter(week.getTime(), weekEndDate.getTime());
    }

    private void createStaticTimeSpanFilters() {
        for (String year : YEARS) {
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.YEAR, Integer.valueOf(year));
            staticTimeSpanFilters
                    .put(year, () -> new TimeSpanFilterBuilder(this::createYearlyFilter, calendar, Calendar.YEAR,
                            calendar.get(Calendar.YEAR)));
        }
    }

    protected static final Ordering<String> ORDERING_BY_LENGTH = new Ordering<String>() {
        @Override
        public int compare(String left, String right) {
            return Ints.compare(right.length(), left.length());
        }
    };

    @Override
    public List<String> parse(List<String> queryWords, SearchParserContext context, boolean addFilter) {
        HashMap<String, Supplier<TimeSpanFilterBuilder>> timeSpanFilters = createDynamicTimeSpanFilters(context);

        timeSpanFilters.putAll(staticTimeSpanFilters);

        String queryString = SearchParser.QUERY_STRING_JOINER.join(queryWords);

        ArrayList<String> timespanFilterPatterns = Lists.newArrayList(timeSpanFilters.keySet());

        Collections.sort(timespanFilterPatterns, ORDERING_BY_LENGTH);
        List<TimeSpanFilterBuilder> timeSpanFilterBuilders = Lists.newArrayList();

        for (String timespanFilterPattern : timespanFilterPatterns) {
            if (!queryString.toLowerCase().contains(timespanFilterPattern)) {
                continue;
            }

            if (addFilter) {
                timeSpanFilterBuilders.add(timeSpanFilters.get(timespanFilterPattern).get());
            }

            queryString = queryString.replaceAll("(?i)" + timespanFilterPattern, "");
        }

        if (addFilter && !timeSpanFilterBuilders.isEmpty()) {
            context.getQueryFilters().addAll(processFilters(timeSpanFilterBuilders, context.getLocale()));
            context.setResponseResolution(
                    mapFilterPatternToResolution(context, SearchParser.QUERY_STRING_JOINER.join(queryWords)));
        }

        return Lists.newArrayList(SearchParser.QUERY_STRING_SPITTER.split(queryString));
    }

    private ResolutionTypes mapFilterPatternToResolution(SearchParserContext context, String queryString) {
        if (queryString.contains(context.getCatalog().getString("day")) ||
                queryString.contains(context.getCatalog().getString("yesterday"))) {
            return ResolutionTypes.DAILY;
        }
        if (queryString.contains(context.getCatalog().getString("week"))) {
            return ResolutionTypes.WEEKLY;
        }
        if (queryString.contains(context.getCatalog().getString("month"))) {
            return ResolutionTypes.MONTHLY;
        }
        if (queryString.contains(context.getCatalog().getString("weekdays"))) {
            return ResolutionTypes.MONTHLY;
        }
        if (queryString.contains(context.getCatalog().getString("weekends"))) {
            return ResolutionTypes.MONTHLY;
        }
        for (String year : TimeSpanParserCommand.YEARS) {
            if (queryString.contains(year)) {
                return ResolutionTypes.YEARLY;
            }
        }
        return ResolutionTypes.MONTHLY;
    }

    private List<RangeFilterBuilder> processFilters(List<TimeSpanFilterBuilder> timeSpanFilterBuilders, Locale locale) {
        List<RangeFilterBuilder> result = Lists.newArrayList();

        List<TimeSpanFilterBuilder> yearlyFilters = Lists.newArrayList();
        List<TimeSpanFilterBuilder> notYearlyFilters = Lists.newArrayList();

        for (TimeSpanFilterBuilder timeSpanFilterBuilder : timeSpanFilterBuilders) {
            if (!timeSpanFilterBuilder.isCombined()) {
                result.add(timeSpanFilterBuilder.getDefaultFilter());
                continue;
            }
            if (Objects.equals(timeSpanFilterBuilder.getCalendarField(), Calendar.YEAR)) {
                yearlyFilters.add(timeSpanFilterBuilder);
            } else {
                notYearlyFilters.add(timeSpanFilterBuilder);
            }
        }

        if (yearlyFilters.size() > 1) {
            // conflict. No time interception
            return Collections.emptyList();
        }

        if (yearlyFilters.isEmpty()) {
            notYearlyFilters.forEach(timeSpanFilterBuilder -> result.add(timeSpanFilterBuilder.getDefaultFilter()));
            return result;
        }

        if (notYearlyFilters.isEmpty()) {
            yearlyFilters.forEach(timeSpanFilterBuilder -> result.add(timeSpanFilterBuilder.getDefaultFilter()));
            return result;
        }

        Calendar date = DateUtils.getCalendar(locale);
        date.set(Calendar.YEAR, yearlyFilters.get(0).getCalendarFieldValue());
        date.set(Calendar.DAY_OF_MONTH, 1);

        notYearlyFilters.forEach(timeSpanFilterBuilder -> {
            date.set(timeSpanFilterBuilder.getCalendarField(), timeSpanFilterBuilder.getCalendarFieldValue());
            result.add(timeSpanFilterBuilder.getFilter(date));
        });

        return result;
    }

    private class TimeSpanFilterBuilder {
        private Function<Calendar, RangeFilterBuilder> filterFunction;
        private Calendar defaultValue;
        private Integer calendarField;
        private Integer calendarFieldValue;

        public TimeSpanFilterBuilder(
                Function<Calendar, RangeFilterBuilder> filterFunction, Calendar defaultValue) {
            this.filterFunction = filterFunction;
            this.defaultValue = defaultValue;
        }

        public TimeSpanFilterBuilder(
                Function<Calendar, RangeFilterBuilder> filterFunction, Calendar defaultValue, Integer calendarField,
                Integer calendarFieldValue) {
            this.filterFunction = filterFunction;
            this.defaultValue = defaultValue;
            this.calendarField = calendarField;
            this.calendarFieldValue = calendarFieldValue;
        }

        public boolean isCombined() {
            return calendarField != null;
        }

        public RangeFilterBuilder getDefaultFilter() {
            return filterFunction.apply(defaultValue);
        }

        public RangeFilterBuilder getFilter(Calendar date) {
            return filterFunction.apply(date);
        }

        public Calendar getDefaultValue() {
            return defaultValue;
        }

        public Integer getCalendarField() {
            return calendarField;
        }

        public Integer getCalendarFieldValue() {
            return calendarFieldValue;
        }
    }
}
