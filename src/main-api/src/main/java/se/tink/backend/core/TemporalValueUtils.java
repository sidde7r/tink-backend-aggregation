package se.tink.backend.core;

import com.google.common.collect.Lists;
import java.util.Date;
import java.util.List;
import se.tink.libraries.date.DateUtils;

public class TemporalValueUtils {
    // A TIMELINE is a list of TemporalValues split into monthly periods and is generated until the current date
    public static <T> List<TemporalValue<T>> convertTemporalValuesToTimeline(List<TemporalValue<T>> filteredTemporalValues) {
        Date today = DateUtils.getToday();

        List<TemporalValue<T>> timeline = Lists.newArrayList();

        for (int i = 0; i < filteredTemporalValues.size(); i++) {
            TemporalValue<T> temporalValue = filteredTemporalValues.get(i);
            timeline.add(temporalValue);

            Date thisDate = temporalValue.getDate();

            long monthsToFlatFill;
            if (i == (filteredTemporalValues.size() - 1)) {
                // Last entry. Flat-fill until today.
                monthsToFlatFill = DateUtils.getCalendarMonthsBetween(thisDate, today);
            } else {
                Date nextDate = filteredTemporalValues.get(i + 1).getDate();
                monthsToFlatFill = DateUtils.getCalendarMonthsBetween(thisDate, nextDate) - 1;
            }

            if (monthsToFlatFill > 0) {
                for (int y = 0; y < monthsToFlatFill; y++) {
                    Date date = DateUtils.addMonths(temporalValue.getDate(), y + 1);
                    TemporalValue<T> temporalValueCopy = new TemporalValue<>(date, temporalValue.getValue());
                    timeline.add(temporalValueCopy);
                }
            }
        }
        return timeline;
    }
}
