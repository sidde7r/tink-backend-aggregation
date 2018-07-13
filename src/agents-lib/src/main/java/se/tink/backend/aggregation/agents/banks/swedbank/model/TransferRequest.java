package se.tink.backend.aggregation.agents.banks.swedbank.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TransferRequest {
    private String amount;
    private String recipientId;
    private String noteToRecipient;
    private String fromAccountId;
    private String noteToSender;
    private String date;

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getRecipientId() {
        return recipientId;
    }

    public void setRecipientId(String recipientId) {
        this.recipientId = recipientId;
    }

    public String getNoteToRecipient() {
        return noteToRecipient;
    }

    public void setNoteToRecipient(String noteToRecipient) {
        this.noteToRecipient = noteToRecipient;
    }

    public String getFromAccountId() {
        return fromAccountId;
    }

    public void setFromAccountId(String fromAccountId) {
        this.fromAccountId = fromAccountId;
    }

    public String getNoteToSender() {
        return noteToSender;
    }

    public void setNoteToSender(String noteToSender) {
        this.noteToSender = noteToSender;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
