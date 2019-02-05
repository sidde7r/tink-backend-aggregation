package se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.text.ParseException;
import java.util.Date;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.amount.Amount;
import se.tink.libraries.date.ThreadSafeDateFormat;

@JsonObject
public class ReservationsEntity {
    private String accountNumber;
    private double amount;
    private String buyCode;
    private String cardNumber;
    private double originalAmount;
    private String originalCurrency;
    private String reservationDate;
    private String sourceAddress;
    private String sourceCountryCode;
    private String sourceLocation;
    private String sourceName;
    private String sourceTelephone;

    @JsonIgnore
    public Transaction toTinkTransaction() {
        return Transaction.builder()
                .setAmount(Amount.inEUR(amount))
                .setDate(dateParsed())
                .setDescription(sourceName)
                .setPending(true)
                .build();
    }

    private Date dateParsed() {
        try {
            return ThreadSafeDateFormat.FORMATTER_MILLISECONDS_WITH_TIMEZONE.parse(reservationDate);
        } catch (ParseException pe) {
            throw new RuntimeException("Failed to parse reservation date", pe);
        }
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public double getAmount() {
        return amount;
    }

    public String getBuyCode() {
        return buyCode;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public double getOriginalAmount() {
        return originalAmount;
    }

    public String getOriginalCurrency() {
        return originalCurrency;
    }

    public String getReservationDate() {
        return reservationDate;
    }

    public String getSourceAddress() {
        return sourceAddress;
    }

    public String getSourceCountryCode() {
        return sourceCountryCode;
    }

    public String getSourceLocation() {
        return sourceLocation;
    }

    public String getSourceName() {
        return sourceName;
    }

    public String getSourceTelephone() {
        return sourceTelephone;
    }
}
