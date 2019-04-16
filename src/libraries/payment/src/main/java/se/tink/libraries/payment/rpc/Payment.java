package se.tink.libraries.payment.rpc;

import se.tink.libraries.amount.Amount;
import se.tink.libraries.payment.enums.PaymentStatus;
import se.tink.libraries.payment.enums.PaymentType;

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
    private PaymentType type;

    public String getCurrency() {
        return currency;
    }

    private String currency;

    public Payment(
            Creditor creditor,
            Debtor debtor,
            Amount amount,
            LocalDate executionDate,
            String currency,
            PaymentType type) {
        this.creditor = creditor;
        this.debtor = debtor;
        this.amount = amount;
        this.executionDate = executionDate;
        this.currency = currency;
        this.id = UUID.randomUUID();
        this.type = type;
        this.status = PaymentStatus.UNDEFINED;
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

    public PaymentType getType() {
        return type;
    }
}
