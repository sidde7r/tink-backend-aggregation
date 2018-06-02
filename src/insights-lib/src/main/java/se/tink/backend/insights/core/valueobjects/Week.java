package se.tink.backend.insights.core.valueobjects;

import java.util.Calendar;
import java.util.Date;

public class Week {
    private Calendar weekStart;
    private Calendar weekEnd;
    private int weekOfYear;

    Week(Calendar weekStart, Calendar weekEnd, int weekOfYear) {
        this.weekStart = weekStart;
        this.weekEnd = weekEnd;
        this.weekOfYear = weekOfYear;
    }

    public static Week of(Calendar weekStart, Calendar weekEnd) {
        return new Week(weekStart, weekEnd, weekStart.get(Calendar.WEEK_OF_YEAR));
    }

    public Date getWeekStartDate() {
        return weekStart.getTime();
    }

    public Date getWeekEndDate() {
        return weekEnd.getTime();
    }

    public int getWeekOfYear() {
        return weekOfYear;
    }
}
