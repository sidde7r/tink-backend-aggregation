package se.tink.backend.aggregation.agents.nxgen.at.banks.raiffeisen.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Date;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TransactionEntity {
    private long id;
    private BalanceEntity balance;
    private Date bookingDate;
    private String paymentReference;
    private String description;

    public long getId() {
        return id;
    }

    @JsonProperty("betrag")
    public BalanceEntity getBalance() {
        return balance;
    }

    @JsonProperty("buchungstag")
    public Date getBookingDate() {
        return bookingDate;
    }

    @JsonProperty("zahlungsreferenz")
    public String getPaymentReference() {
        return paymentReference;
    }

    @JsonProperty("verwendungszweckZeile1")
    public String getDescription() {
        return description;
    }
}
