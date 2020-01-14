package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.entity;

import java.time.LocalDate;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TransactionEntity {

    private String bookingDate;
    private String transactionDate;
    private String creditDebitIndicator;
    private String entryReference;
    private List<String> remittanceInformation;
    private String status;
    private TransactionAmount transactionAmount;

    public LocalDate getBookingDate() {
        return LocalDate.parse(bookingDate);
    }

    public String getCreditDebitIndicator() {
        return creditDebitIndicator;
    }

    public String getEntryReference() {
        return entryReference;
    }

    public List<String> getRemittanceInformation() {
        return remittanceInformation;
    }

    public String getStatus() {
        return status;
    }

    public TransactionAmount getTransactionAmount() {
        return transactionAmount;
    }
}
