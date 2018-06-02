package se.tink.backend.insights.core.valueobjects;

import java.util.Date;

public class EInvoice {
    private EInvoiceId eInvoiceId;
    private Amount amount;
    private Date dueDate;
    private String sourceMessage;

    EInvoice(EInvoiceId eInvoiceId, Amount amount, Date dueDate, String sourceMessage) {
        this.eInvoiceId = eInvoiceId;
        this.amount = amount;
        this.dueDate = dueDate;
        this.sourceMessage = sourceMessage;
    }

    public EInvoiceId geteInvoiceId() {
        return eInvoiceId;
    }

    public Amount getAmount() {
        return amount;
    }

    public Date getDueDate() {
        return dueDate;
    }

    public String getSourceMessage() {
        return sourceMessage;
    }

    public static EInvoice of(EInvoiceId eInvoiceId, Amount amount, Date dueDate, String sourceMessage) {
        return new EInvoice(eInvoiceId, amount, dueDate, sourceMessage);
    }
}
