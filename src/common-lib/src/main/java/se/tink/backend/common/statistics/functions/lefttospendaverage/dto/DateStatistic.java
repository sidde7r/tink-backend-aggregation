package se.tink.backend.common.statistics.functions.lefttospendaverage.dto;

import java.text.ParseException;
import java.util.Date;
import java.util.Objects;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import se.tink.backend.core.Statistic;
import se.tink.libraries.date.ThreadSafeDateFormat;

public class DateStatistic {
    private DateTime dateTime;
    private final Statistic statistic;

    public DateStatistic(Statistic statistic) throws ParseException {
        this.statistic = statistic;
        Date date = ThreadSafeDateFormat.FORMATTER_DAILY.parse(statistic.getDescription());
        this.dateTime = new DateTime(date.getTime()).withZoneRetainFields(DateTimeZone.UTC);
    }

    public DateStatistic(DateStatistic dateStatistic) {
        this(dateStatistic, 0);
    }

    public DateStatistic(DateStatistic dateStatistic, int addedDays) {
        this.dateTime = new DateTime(dateStatistic.dateTime).plusDays(addedDays);

        Statistic copiedStatistic = dateStatistic.statistic;

        Statistic statistic = new Statistic();

        if (!Objects.equals(addedDays, 0)) {
            statistic.setDescription(ThreadSafeDateFormat.FORMATTER_DAILY.format(this.dateTime.toDate()));
        } else {
            statistic.setDescription(copiedStatistic.getDescription());
        }

        statistic.setPayload(copiedStatistic.getPayload());
        statistic.setPeriod(copiedStatistic.getPeriod());
        statistic.setResolution(copiedStatistic.getResolution());
        statistic.setType(copiedStatistic.getType());
        statistic.setUserId(copiedStatistic.getUserId());
        statistic.setValue(copiedStatistic.getValue());

        this.statistic = statistic;
    }

    public DateStatistic(DateStatistic dateStatistic, String period, DateTime dateTime, double value) {
        this(dateStatistic, 0);
        this.dateTime = dateTime;
        this.getStatistic().setPeriod(period);
        this.getStatistic().setDescription(ThreadSafeDateFormat.FORMATTER_DAILY.format(dateTime.toDate()));
        this.getStatistic().setValue(value);
    }

    public DateTime getDateTime() {
        return dateTime;
    }

    public Statistic getStatistic() {
        return statistic;
    }
}
