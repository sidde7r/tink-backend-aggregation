package se.tink.backend.grpc.v1.converter.periods;

import java.util.Calendar;
import se.tink.backend.grpc.v1.converter.Converter;
import se.tink.grpc.v1.models.PeriodDescription;
import se.tink.libraries.date.ThreadSafeDateFormat;

public class GrpcPeriodDescriptionToStringConverter implements Converter<PeriodDescription, String> {
    @Override
    public String convertFrom(PeriodDescription period) {
        Calendar calendar = Calendar.getInstance();
        if (period.getWeek() > 0) {
            validateWeeklyPeriod(period);
            calendar.set(Calendar.YEAR, period.getYear());
            calendar.set(Calendar.WEEK_OF_YEAR, period.getWeek());
            return ThreadSafeDateFormat.FORMATTER_WEEKLY.format(calendar.getTime());
        }

        if (period.getDay() > 0) {
            validateDailyPeriod(period);
            calendar.set(period.getYear(), period.getMonth() - 1, period.getDay());
            return ThreadSafeDateFormat.FORMATTER_DAILY.format(calendar.getTime());
        }

        if (period.getMonth() > 0) {
            validateMonthlyPeriod(period);
            calendar.set(period.getYear(), period.getMonth() - 1, 1);
            return ThreadSafeDateFormat.FORMATTER_MONTHLY.format(calendar.getTime());
        }

        calendar.set(Calendar.YEAR, period.getYear());
        return ThreadSafeDateFormat.FORMATTER_YEARLY.format(calendar.getTime());
    }

    private void validateWeeklyPeriod(PeriodDescription period) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, period.getYear());
        if (period.getMonth() != 0 || period.getDay() != 0 || period.getWeek() <= 0 || period.getWeek() > calendar
                .getWeeksInWeekYear()) {
            throw new IllegalArgumentException("Invalid weekly period.");
        }
    }

    private void validateDailyPeriod(PeriodDescription period) {
        if (period.getWeek() != 0 || period.getMonth() < 1 || period.getMonth() > 12) {
            throw new IllegalArgumentException("Invalid daily period.");
        }
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, period.getYear());
        calendar.set(Calendar.MONTH, period.getMonth() - 1);
        if (period.getDay() < 1 || period.getDay() > calendar.getActualMaximum(Calendar.DAY_OF_MONTH)) {
            throw new IllegalArgumentException("Invalid daily period.");
        }
    }

    private void validateMonthlyPeriod(PeriodDescription period) {
        if (period.getDay() != 0 || period.getWeek() != 0 || period.getMonth() < 1 || period.getMonth() > 12) {
            throw new IllegalArgumentException("Invalid monthly period.");
        }
    }
}
