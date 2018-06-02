package se.tink.backend.common.application.mortgage;

import java.util.Calendar;
import java.util.Date;
import se.tink.libraries.date.DateUtils;
import se.tink.libraries.date.ThreadSafeDateFormat;
import se.tink.libraries.i18n.Catalog;

public class CompileAndSendReportCommand {

    private static final ThreadSafeDateFormat MONTH_FORMAT = new ThreadSafeDateFormat("MMMMM yyyy");
    private static final ThreadSafeDateFormat PERIOD_FORMAT = ThreadSafeDateFormat.FORMATTER_DAILY;

    private final Date startDate;
    private final Date endDate;
    private final String description;

    private CompileAndSendReportCommand(Date startDate, Date endDate, String description) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.description = description;
    }

    public static CompileAndSendReportCommand forLastCompleteMonth() {
        return forLastCompleteMonth(new Date());
    }

    public static CompileAndSendReportCommand forLastCompleteMonth(Date reference) {
        Calendar calendar = DateUtils.getCalendar(reference);
        calendar.set(Calendar.DATE, 1);
        calendar.add(Calendar.DAY_OF_MONTH, -1);
        Date lastDateOfPreviousMonth = DateUtils.setInclusiveEndTime(calendar.getTime());

        calendar.set(Calendar.DATE, 1);
        Date firstDateOfPreviousMonth = DateUtils.setInclusiveStartTime(calendar.getTime());

        String description = getMonthDescription(firstDateOfPreviousMonth);

        return new CompileAndSendReportCommand(firstDateOfPreviousMonth, lastDateOfPreviousMonth, description);
    }

    // `month` needs to be on the format YYYY-MM
    public static CompileAndSendReportCommand forMonth(String month) {
        Date firstDateOfMonth = DateUtils.getFirstDateFromPeriod(month);
        Date lastDateOfMonth = DateUtils.getLastDateFromPeriod(month);

        String description = getMonthDescription(firstDateOfMonth);

        return new CompileAndSendReportCommand(firstDateOfMonth, lastDateOfMonth, description);
    }

    public static CompileAndSendReportCommand forPeriod(Date startDate, Date endDate) {
        String description = Catalog
                .format("{0} t.o.m. {1}", PERIOD_FORMAT.format(startDate), PERIOD_FORMAT.format(endDate));

        return new CompileAndSendReportCommand(startDate, endDate, description);
    }

    public Date getStartDate() {
        return startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public boolean isDateIncluded(Date date) {
        return !startDate.after(date) && !endDate.before(date);
    }

    public String getDescription() {
        return description;
    }

    private static String getMonthDescription(Date date) {
        String description = MONTH_FORMAT.format(date);
        return description.substring(0, 1).toUpperCase() + description.substring(1);
    }
}
