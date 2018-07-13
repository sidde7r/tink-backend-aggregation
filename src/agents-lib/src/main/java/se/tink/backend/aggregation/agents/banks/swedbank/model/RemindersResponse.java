package se.tink.backend.aggregation.agents.banks.swedbank.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RemindersResponse extends AbstractResponse {
    
    private ReminderDetails rejectedPayments;
    private ReminderDetails unsignedPayments;
    private ReminderDetails unsignedTransfers;
    private ReminderDetails incomingEinvoices;
    
    public ReminderDetails getRejectedPayments() {
        return rejectedPayments;
    }
    public void setRejectedPayments(ReminderDetails rejectedPayments) {
        this.rejectedPayments = rejectedPayments;
    }
    public ReminderDetails getUnsignedPayments() {
        return unsignedPayments;
    }
    public void setUnsignedPayments(ReminderDetails unsignedPayments) {
        this.unsignedPayments = unsignedPayments;
    }
    public ReminderDetails getUnsignedTransfers() {
        return unsignedTransfers;
    }
    public void setUnsignedTransfers(ReminderDetails unsignedTransfers) {
        this.unsignedTransfers = unsignedTransfers;
    }
    public ReminderDetails getIncomingEinvoices() {
        return incomingEinvoices;
    }
    public void setIncomingEinvoices(ReminderDetails incomingEinvoices) {
        this.incomingEinvoices = incomingEinvoices;
    }
}
