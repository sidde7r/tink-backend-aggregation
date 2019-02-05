package se.tink.backend.aggregation.agents.nxgen.no.banks.dnb.accounts.checkingaccount.entities;


import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.base.Strings;
import java.util.Date;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.amount.Amount;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class TransactionEntity {
    private long accountingDate;
    private long bookedDate;
    private double amount;
    private String textCode;
    private String description;
    private String shortDescription;
    private String textLine1;
    private String textLine2;
    private String textLine3;
    private String status;
    private Long valueDate;
    private String journalReference;
    private String reference;
    private Boolean crossborder;

    public long getAccountingDate() {
        return accountingDate;
    }

    public long getBookedDate() {
        return bookedDate;
    }

    public double getAmount() {
        return amount;
    }

    public String getTextCode() {
        return textCode;
    }

    public String getDescription() {
        if (!Strings.isNullOrEmpty(textLine1)) {
            return textLine1;
        } else if (!Strings.isNullOrEmpty(textLine2)) {
            return textLine2;
        } else if (!Strings.isNullOrEmpty(textLine3)) {
            return textLine3;
        } else if (!Strings.isNullOrEmpty(description)) {
            return description;
        }

        return "Unknown transaction";
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public String getTextLine1() {
        return textLine1;
    }

    public String getTextLine2() {
        return textLine2;
    }

    public String getTextLine3() {
        return textLine3;
    }

    public String getStatus() {
        return status;
    }

    public Long getValueDate() {
        return valueDate;
    }

    public String getJournalReference() {
        return journalReference;
    }

    public String getReference() {
        return reference;
    }

    public Boolean getCrossborder() {
        return crossborder;
    }

    public Transaction toTinkTransaction() {
        return Transaction.builder()
                .setAmount(Amount.inNOK(amount))
                .setDate(new Date(getAccountingDate()))
                .setDescription(getDescription())
                .setPending(getValueDate() == null)
                .build();
    }

}
