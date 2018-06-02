package se.tink.backend.aggregation.agents.nxgen.fi.banks.alandsbanken.fetcher.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Strings;
import java.util.Date;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.core.Amount;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AlandsBankenTransaction {
    private double amount;
    private String currency;
    private Date dueDate;
    private boolean incoming; //unverified... Not seen coming from Finnish Ålandsbanken backend
    private String ownNote; //unverified... Not seen coming from Finnish Ålandsbanken backend
    @JsonProperty("recieverName") // Typo in their api
    private String receiverName;
    private String textCode;
    private String transactionId;

//    Unused fields, but possibly of interest.
//    private Date bookingDate;
//    private double originalAmount;
//    private String originalCurrency;
//    private String accountNumber;
//    private String reference;
//    private boolean ownRegistered;
//    private boolean copyable;
//    private boolean sepaTransaction;
//    private String periodicity;
//    private Date endDate;

    public Transaction toTinkAccount() {
        return Transaction.builder()
                .setAmount(Amount.inEUR(amount))
                .setDescription(createDescription())
                .setDate(dueDate)
                .build();
    }

    private String createDescription() {
        //Hypothesis: this logic is here for Swedish Ålandsbanken
        //Finnish Ålandsbanken will return receiverName (if present)
        if (!Strings.isNullOrEmpty(ownNote)) {
            return ownNote;
        } else if (!incoming && !Strings.isNullOrEmpty(receiverName)) {
            return receiverName;
        }

        return textCode;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public Date getDueDate() {
        return dueDate;
    }

    @JsonFormat(pattern = "yyyyMMdd")
    public void setDueDate(Date dueDate) {
        this.dueDate = dueDate;
    }

    public boolean isIncoming() {
        return incoming;
    }

    public void setIncoming(boolean incoming) {
        this.incoming = incoming;
    }

    public String getOwnNote() {
        return ownNote;
    }

    public void setOwnNote(String ownNote) {
        this.ownNote = ownNote;
    }

    public String getReceiverName() {
        return receiverName;
    }

    public void setReceiverName(String receiverName) {
        this.receiverName = receiverName;
    }

    public String getTextCode() {
        return textCode;
    }

    public void setTextCode(String textCode) {
        this.textCode = textCode;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }
}
