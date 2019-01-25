package se.tink.backend.aggregation.agents.banks.crosskey.responses;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Strings;
import java.text.ParseException;
import java.util.Date;
import se.tink.backend.system.rpc.Transaction;
import se.tink.libraries.date.DateUtils;
import se.tink.libraries.date.ThreadSafeDateFormat;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TransactionResponse {
    private double amount;
    private String currency;
    private Date dueDate;
    private boolean incoming;
    private String ownNote;
    @JsonProperty("recieverName") // Typo in their api
    private String receiverName;
    private String textCode;
    private String transactionId;

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

    public Date getDueDate() throws Exception {
        if (dueDate.getTime() > 0) {
            return dueDate;
        }

        throw new Exception();
    }

    public void setDueDate(String date) throws ParseException {
        Date parsedDate = ThreadSafeDateFormat.FORMATTER_INTEGER_DATE.parse(date);
        dueDate = DateUtils.flattenTime(parsedDate);
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

    /* Helpers */
    @JsonIgnore
    public String getDescription() {
        if (!Strings.isNullOrEmpty(ownNote)) {
            return ownNote;
        } else if (!incoming && !Strings.isNullOrEmpty(receiverName)) {
            return receiverName;
        }

        return textCode;
    }

    public Transaction toTinkTransaction() throws Exception {
        Transaction tinkTransaction = new Transaction();

        tinkTransaction.setAmount(amount);
        tinkTransaction.setDescription(getDescription());
        tinkTransaction.setDate(getDueDate());

        return tinkTransaction;
    }
}
