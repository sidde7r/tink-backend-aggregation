package se.tink.libraries.payment.rpc;

import com.google.common.collect.ImmutableList;
import java.time.LocalDate;
import java.util.UUID;
import org.iban4j.IbanUtil;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.AccountIdentifier.Type;
import se.tink.libraries.amount.Amount;
import se.tink.libraries.pair.Pair;
import se.tink.libraries.payment.enums.PaymentStatus;
import se.tink.libraries.payment.enums.PaymentType;

public class Payment {
    private static ImmutableList<String> sepaCountriesWithEur =
            ImmutableList.of(
                    "AT", "BE", "CY", "DE", "EE", "ES", "FI", "FR", "GR", "IE", "IT", "LT", "LU",
                    "LV", "MT", "NL", "PT", "SI", "SK");
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

    public void setStatus(PaymentStatus status) {
        this.status = status;
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

    public Pair<AccountIdentifier.Type, AccountIdentifier.Type> getCreditorAndDebtorAccountType() {
        return new Pair<>(debtor.getAccountIdentifierType(), creditor.getAccountIdentifierType());
    }

    public boolean isSepa() {
        if (debtor.getAccountIdentifierType() == Type.IBAN
                && creditor.getAccountIdentifierType() == Type.IBAN) {
            if (sepaCountriesWithEur.contains(IbanUtil.getCountryCode(debtor.getAccountNumber()))
                    && sepaCountriesWithEur.contains(
                            IbanUtil.getCountryCode(creditor.getAccountNumber()))) return true;
        }
        return false;
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

        public Payment build() {
            return new Payment(this);
        }
    }
}
