package se.tink.agent.sdk.models.transaction;

import java.time.Instant;
import java.time.LocalDate;
import lombok.Getter;

@Getter
public class TransactionDate {
    private final TransactionDateType type;

    // Either a date or a datetime depending on what the bank offers.
    private final LocalDate date;
    private final Instant dateTime;

    TransactionDate(TransactionDateType type, LocalDate date, Instant dateTime) {
        this.type = type;
        this.date = date;
        this.dateTime = dateTime;
    }

    public static TransactionDate fromDate(TransactionDateType type, LocalDate date) {
        return new TransactionDate(type, date, null);
    }

    public static TransactionDate fromDateTime(TransactionDateType type, Instant dateTime) {
        return new TransactionDate(type, null, dateTime);
    }
}
