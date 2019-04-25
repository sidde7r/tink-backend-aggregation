package se.tink.libraries.payment.rpc;

import com.fasterxml.jackson.core.type.TypeReference;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import se.tink.backend.aggregation.nxgen.storage.TemporaryStorage;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.amount.Amount;
import se.tink.libraries.pair.Pair;
import se.tink.libraries.payment.enums.PaymentStatus;
import se.tink.libraries.payment.enums.PaymentType;

public class Payment {
    private Creditor creditor;
    private Debtor debtor;
    private Amount amount;
    private LocalDate executionDate;
    private UUID id;
    private String uniqueId;
    private PaymentStatus status;
    private PaymentType type;
    private String currency;
    private Reference reference;
    private TemporaryStorage temporaryStorage;

    private Payment(Builder builder) {
        this.creditor = builder.creditor;
        this.debtor = builder.debtor;
        this.amount = builder.amount;
        this.executionDate = builder.executionDate;
        this.currency = builder.currency;
        this.type = builder.type;
        this.status = builder.status;
        this.uniqueId = builder.uniqueId;
        this.reference = builder.reference;
        this.id = UUID.randomUUID();
        this.temporaryStorage = builder.getTransientStorage();
    }

    public String getCurrency() {
        return currency;
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

    public String getUniqueId() {
        return uniqueId;
    }

    public PaymentType getType() {
        return type;
    }

    public Reference getReference() {
        return reference;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
    }

    public Pair<AccountIdentifier.Type, AccountIdentifier.Type> getCreditorAndDebtorAccountType() {
        return new Pair<>(debtor.getAccountIdentifierType(), creditor.getAccountIdentifierType());
    }

    public String getFromTemporaryStorage(String key) {
        return temporaryStorage.get(key);
    }

    public <T> Optional<T> getFromTemporaryStorage(String key, Class<T> valueType) {
        return temporaryStorage.get(key, valueType);
    }

    public <T> Optional<T> getFromTemporaryStorage(String key, TypeReference<T> valueType) {
        return temporaryStorage.get(key, valueType);
    }

    public static class Builder {
        private Creditor creditor;
        private Debtor debtor;
        private Amount amount;
        private LocalDate executionDate;
        private String uniqueId;
        private PaymentStatus status = PaymentStatus.CREATED;
        private PaymentType type = PaymentType.UNDEFINED;
        private String currency;
        private Reference reference;
        protected final TemporaryStorage temporaryStorage = new TemporaryStorage();

        public Builder withCreditor(Creditor creditor) {
            this.creditor = creditor;
            return this;
        }

        public Builder withDebtor(Debtor debtor) {
            this.debtor = debtor;
            return this;
        }

        public Builder withAmount(Amount amount) {
            this.amount = amount;
            return this;
        }

        public Builder withExecutionDate(LocalDate executionDate) {
            this.executionDate = executionDate;
            return this;
        }

        public Builder withUniqueId(String uniqueId) {
            this.uniqueId = uniqueId;
            return this;
        }

        public Builder withStatus(PaymentStatus status) {
            this.status = status;
            return this;
        }

        public Builder withType(PaymentType type) {
            this.type = type;
            return this;
        }

        public Builder withCurrency(String currency) {
            this.currency = currency;
            return this;
        }

        public Builder withReference(Reference reference) {
            this.reference = reference;
            return this;
        }

        public <K> Builder putInTemporaryStorage(String key, K value) {
            temporaryStorage.put(key, value);
            return this;
        }

        private TemporaryStorage getTransientStorage() {
            return temporaryStorage;
        }

        public Payment build() {
            return new Payment(this);
        }
    }
}
