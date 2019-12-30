package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TransactionEntity {
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date bookingDate;

    private String creditDebitIndicator;
    private String entryReference;
    private List<String> remittanceInformation;
    private String status;
    private TransactionAmount transactionAmount;

    public Date getBookingDate() {
        return bookingDate;
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
