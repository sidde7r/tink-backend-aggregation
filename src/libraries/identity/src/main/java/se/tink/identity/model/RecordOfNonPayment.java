package se.tink.libraries.identity.model;

import java.util.Calendar;
import java.util.Date;
import se.tink.libraries.date.DateUtils;

public class RecordOfNonPayment {
    private String name;
    private double amount;
    private Date registeredDate;

    // payment notes are valid for 3 years
    private final int VALIDITY_IN_YEARS = 3;

    public RecordOfNonPayment(String name, double amount, Date registeredDate) {
        this.name = name;
        this.amount = amount;
        this.registeredDate = registeredDate;
    }

    public String getName() {
        return name;
    }

    public double getAmount() {
        return amount;
    }

    public Date getRegisteredDate() {
        return registeredDate;
    }

    public static RecordOfNonPayment of(String name, double amount, Date registeredDate) {
        return new RecordOfNonPayment(name, amount, registeredDate);
    }

    public boolean isPaymentNoteValid() {
        return DateUtils.isDateDiffLessThanPeriod(registeredDate, new Date(), Calendar.YEAR, VALIDITY_IN_YEARS);
    }

}
