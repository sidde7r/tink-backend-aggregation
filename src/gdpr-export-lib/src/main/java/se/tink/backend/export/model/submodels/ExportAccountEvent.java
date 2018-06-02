package se.tink.backend.export.model.submodels;

import se.tink.backend.export.helper.DefaultSetter;
import se.tink.libraries.date.DateUtils;

public class ExportAccountEvent implements DefaultSetter {

    private final String date; //Date format is yyyymmdd as an integer
    private final String accountNumber;
    private final String name;
    private final String balance;

    public ExportAccountEvent(Integer date, String accountNumber, String name, Double balance) {
        this.date = notNull(DateUtils.fromInteger(date));
        this.accountNumber = notNull(accountNumber);
        this.name = notNull(name);
        this.balance = notNull(balance);
    }

    public String getDate() {
        return date;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getName() {
        return name;
    }

    public String getBalance() {
        return balance;
    }

    private String integerDateToDate(Integer intDate) {
        if (intDate == null) {
            return "";
        }
        String asString = notNull(intDate);

        // Magic number 8 is due to format yyyymmdd
        if (asString.length() < 8) {
            return "";
        }

        String year = asString.substring(0,4);
        String month = asString.substring(4, 6);
        String day = asString.substring(6, 8);

        return year + "-" + month + "-" + day;
    }

}
