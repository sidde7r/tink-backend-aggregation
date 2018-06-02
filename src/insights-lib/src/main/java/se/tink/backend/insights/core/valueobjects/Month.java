package se.tink.backend.insights.core.valueobjects;

import java.util.Calendar;

public class Month {
    private Calendar monthStart;
    private Calendar monthEnd;
    private int monthOfYear;

    Month(Calendar monthStart, Calendar monthEnd, int monthOfYear) {
        this.monthStart = monthStart;
        this.monthEnd = monthEnd;
        this.monthOfYear = monthOfYear;
    }

    public static Month of(Calendar monthStart, Calendar monthEnd) {
        return new Month(monthStart, monthEnd, monthStart.get(Calendar.MONTH));
    }

    public Calendar getMonthStart() {
        return monthStart;
    }

    public Calendar getMonthEnd() {
        return monthEnd;
    }

    public int getMonthOfYear() {
        return monthOfYear;
    }
}
