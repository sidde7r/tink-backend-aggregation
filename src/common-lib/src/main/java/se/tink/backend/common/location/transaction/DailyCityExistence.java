package se.tink.backend.common.location.transaction;

import se.tink.libraries.date.ThreadSafeDateFormat;

import java.text.ParseException;
import java.util.Date;

public class DailyCityExistence {
    private String city;
    private int numTransactions;
    private Date date;

    public DailyCityExistence (String city, int numTransactions, String date) {
        this.city = city;
        this.numTransactions = numTransactions;
        this.date = convertDate(date);
    }

    public String getDateString() {
        return ThreadSafeDateFormat.FORMATTER_DAILY.format(date);
    }

    public Date getDate() {
        return date;
    }

    public int getNumTransactions() {
        return numTransactions;
    }

    public String getCity() {
        return city;
    }

    private Date convertDate(String date) {
        try {
            return ThreadSafeDateFormat.FORMATTER_DAILY.parse(date);
        } catch (ParseException e) {
            return null;
        }
    }
}
