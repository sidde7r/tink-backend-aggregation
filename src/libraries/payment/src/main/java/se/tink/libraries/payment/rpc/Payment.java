package se.tink.libraries.payment.rpc;

import se.tink.libraries.amount.Amount;
import se.tink.libraries.payment.enums.PaymentStatus;

import java.time.LocalDate;
import java.util.UUID;

public class Payment {
    private Creditor creditor;
    private Debtor debtor;
    private Amount amount;
    private LocalDate executionDate;
    private UUID id;
    private String providerId;
    private PaymentStatus status;

    public Payment(Creditor creditor, Debtor debtor, Amount amount, LocalDate executionDate) {
        this.creditor = creditor;
        this.debtor = debtor;
        this.amount = amount;
        this.executionDate = executionDate;
        this.id = UUID.randomUUID();
        this.setStatus(PaymentStatus.UNDEFINED);
    }

    public Creditor getCreditor() {
        return creditor;
    }

    public Debtor getDebtor() {
        return debtor;
    }

    public Amount getAmount() {
        return amount;
    }

    public UUID getId() {
        return id;
    }

    public LocalDate getExecutionDate() {
        return executionDate;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
    }

    public String getProviderId() {
        return providerId;
    }

    public void setProviderId(String providerId) {
        this.providerId = providerId;
    }
}
