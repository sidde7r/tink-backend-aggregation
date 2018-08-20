package se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.fetcher.entities;

import java.util.Date;
import java.util.List;

public final class TransactionEntity {
    private Double amount;
    private String currency;
    private Date bookingDate; // greater than or equal to "date"
    private Date date; // less than or equal to "bookingDate"
    private List<String> description;

    public Double getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    public Date getBookingDate() {
        return bookingDate;
    }

    public Date getDate() {
        return date;
    }

    public List<String> getDescription() {
        return description;
    }
}
