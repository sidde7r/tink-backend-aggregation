package se.tink.backend.core;

import com.google.common.collect.Lists;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.joda.time.DateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.assertEquals;

@RunWith(JUnitParamsRunner.class)
public class TemporalValueUtilsTest {

    @Test
    @Parameters(method = "temporalValuesToTimelineParams")
    public void convertTemporalValuesToTimeline(List<Date> dates, List<Integer> values,
            List<Date> expectedDates, List<Integer> expectedValues) {

        List<TemporalValue<Integer>> input = createTemporalValues(dates, values);
        List<TemporalValue<Integer>> expectedOutput = createTemporalValues(expectedDates, expectedValues);

        List<TemporalValue<Integer>> actualOutput = TemporalValueUtils.convertTemporalValuesToTimeline(input);

        // Assert correct flat filled months in between
        IntStream.range(0, expectedValues.size()).forEach( idx -> {
            assertEquals(expectedOutput.get(idx).getDate(), actualOutput.get(idx).getDate());
            assertEquals(expectedOutput.get(idx).getValue(), actualOutput.get(idx).getValue());
        });

        // Assert length of timeline
        YearMonth m1 = YearMonth.from(expectedOutput.get(0).getDate().toInstant().atZone(TimeZone.getTimeZone("CET").toZoneId()));
        YearMonth m2 = YearMonth.from((new Date()).toInstant().atZone(TimeZone.getTimeZone("CET").toZoneId()));
        assertEquals(actualOutput.size(), m1.until(m2, ChronoUnit.MONTHS) + 1); // Until does not include the months of m1 itself, so + 1

    }

    private List<TemporalValue<Integer>> createTemporalValues(List<Date> dates, List<Integer> values) {
        List<TemporalValue<Integer>> input = Lists.newArrayList();
        IntStream.range(0, dates.size()).forEach( idx -> {
            input.add(new TemporalValue<>(dates.get(idx), values.get(idx)));
        });

        return input;
    }

    private Object[] temporalValuesToTimelineParams() {
        // Test dates
        List<String> dateStrings = Lists.newArrayList(
                "2016-02-15","2016-03-20", "2016-04-24", "2016-08-18");
        List<Date> dates = stringsToDates(dateStrings);

        // Expected dates
        List<String> expectedDateStrings = Lists.newArrayList(
                "2016-02-15","2016-03-20", "2016-04-24", "2016-05-24",
                "2016-06-24", "2016-07-24", "2016-08-18");
        List<Date> expectedDates = stringsToDates(expectedDateStrings);

        return new Object[][]{
                {
                    dates, Lists.newArrayList(1, 2, 4, 6), expectedDates, Lists.newArrayList(1, 2, 4, 4, 4, 4, 6)
                }
        };
    }

    private List<Date> stringsToDates(List<String> dateStrings) {
        return dateStrings.stream().map(s -> new DateTime(DateTime.parse(s)).toDate())
                .collect(Collectors.toList());

    }

}