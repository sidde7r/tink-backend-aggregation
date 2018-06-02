package se.tink.backend.rpc;

import java.util.Date;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.backend.core.Amount;

public class UpdateTransferRequest {
    private Amount amount;
    private AccountIdentifier destination;
    private String destinationMessage;
    private AccountIdentifier source;
    private String sourceMessage;
    private Date dueDate;

    public UpdateTransferRequest() {
    }

    public UpdateTransferRequest(Amount amount, AccountIdentifier destination, String destinationMessage,
            AccountIdentifier source, String sourceMessage, Date dueDate) {
        this.amount = amount;
        this.destination = destination;
        this.destinationMessage = destinationMessage;
        this.source = source;
        this.sourceMessage = sourceMessage;
        this.dueDate = dueDate;
    }

    public Amount getAmount() {
        return amount;
    }

    public void setAmount(Amount amount) {
        this.amount = amount;
    }

    public AccountIdentifier getDestination() {
        return destination;
    }

    public void setDestination(AccountIdentifier destination) {
        this.destination = destination;
    }

    public String getDestinationMessage() {
        return destinationMessage;
    }

    public void setDestinationMessage(String destinationMessage) {
        this.destinationMessage = destinationMessage;
    }

    public AccountIdentifier getSource() {
        return source;
    }

    public void setSource(AccountIdentifier source) {
        this.source = source;
    }

    public String getSourceMessage() {
        return sourceMessage;
    }

    public void setSourceMessage(String sourceMessage) {
        this.sourceMessage = sourceMessage;
    }

    public Date getDueDate() {
        return dueDate;
    }

    public void setDueDate(Date dueDate) {
        this.dueDate = dueDate;
    }
}
