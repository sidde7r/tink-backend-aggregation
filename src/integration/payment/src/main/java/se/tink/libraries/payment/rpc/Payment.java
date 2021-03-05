package se.tink.libraries.payment.rpc;

import com.google.common.collect.ImmutableList;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;
import org.iban4j.IbanUtil;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.AccountIdentifier.Type;
import se.tink.libraries.amount.Amount;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.enums.MarketCode;
import se.tink.libraries.pair.Pair;
import se.tink.libraries.payment.enums.PaymentStatus;
import se.tink.libraries.payment.enums.PaymentType;
import se.tink.libraries.payments.common.model.PaymentScheme;
import se.tink.libraries.transfer.rpc.ExecutionRule;
import se.tink.libraries.transfer.rpc.Frequency;
import se.tink.libraries.transfer.rpc.PaymentServiceType;
import se.tink.libraries.transfer.rpc.RemittanceInformation;

public class Payment {
    private static ImmutableList<String> sepaCountriesWithEur =
            ImmutableList.of(
                    "AT",
                    "BE",
                    "CY",
                    "DE",
                    "EE",
                    "ES",
                    "FI",
                    "FR",
                    "GR",
                    "IE",
                    "IT",
                    "LT",
                    "LU",
                    "LV",
                    "MT",
                    "NL",
                    "PT",
                    "SI",
                    "SK",
                    // This is temporary fix for pt-caixa-ob, I am working on a better solution
                    "GB");
    private Creditor creditor;
    private Debtor debtor;
    private Amount amount;
    // TODO rename back to amount after removing `amount` field
    private ExactCurrencyAmount exactCurrencyAmount;
    private LocalDate executionDate;
    private UUID id;
    private String uniqueId;
    private PaymentStatus status;
    private PaymentType type;
    private String currency;
    private PaymentScheme paymentScheme;
    /** @deprecated (20200828, remittanceInforation should be used instead, to be removed later) */
    @Deprecated private Reference reference;

    private final RemittanceInformation remittanceInformation;
    private PaymentServiceType paymentServiceType;
    private Frequency frequency;
    private LocalDate startDate;
    private LocalDate endDate;
    private ExecutionRule executionRule;

    private Payment(Builder builder) {
        this.creditor = builder.creditor;
        this.debtor = builder.debtor;
        this.amount = builder.amount;
        this.exactCurrencyAmount = builder.exactCurrencyAmount;
        this.executionDate = builder.executionDate;
        this.currency = builder.currency;
        this.type = builder.type;
        this.status = builder.status;
        this.uniqueId = builder.uniqueId;
        this.reference = builder.reference;
        this.id = UUID.randomUUID();
        this.remittanceInformation = builder.remittanceInformation;
        this.paymentScheme = builder.paymentScheme;
        this.paymentServiceType = builder.paymentServiceType;
        this.frequency = builder.frequency;
        this.startDate = builder.startDate;
        this.endDate = builder.endDate;
        this.executionRule = builder.executionRule;
    }

    /*
       This method is used by UK OpenBanking for EndToEndIdentification field since
       there the max allowed length for id is 31
       From the Docs: The Faster Payments Scheme can only access 31 characters for
       the EndToEndIdentification field.
    */
    public String getUniqueIdForUKOPenBanking() {
        if (uniqueId.length() > 31) {
            return uniqueId.substring(0, 31);
        }

        return uniqueId;
    }

    // TODO Double Check: This should return the currency value from exactCurrencyAmount?
    public String getCurrency() {
        return currency;
    }

    public Creditor getCreditor() {
        return creditor;
    }

    public Debtor getDebtor() {
        return debtor;
    }

    public ExactCurrencyAmount getExactCurrencyAmount() {
        return new ExactCurrencyAmount(BigDecimal.valueOf(amount.getValue()), amount.getCurrency());
    }

    // TODO: This will be renamed to `getAmount` after we refactored every agent
    public ExactCurrencyAmount getExactCurrencyAmountFromField() {
        return exactCurrencyAmount;
    }

    public UUID getId() {
        return id;
    }

    public LocalDate getExecutionDate() {
        return executionDate;
    }

    public void setExecutionDate(LocalDate date) {
        this.executionDate = date;
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

    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }

    public PaymentType getType() {
        return type;
    }

    /** @deprecated (20200828, remittanceInforation should be used instead, to be removed later) */
    @Deprecated
    public Reference getReference() {
        return reference;
    }

    public PaymentScheme getPaymentScheme() {
        return paymentScheme;
    }

    public RemittanceInformation getRemittanceInformation() {
        return remittanceInformation;
    }

    public PaymentServiceType getPaymentServiceType() {
        return paymentServiceType;
    }

    public Frequency getFrequency() {
        return frequency;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public ExecutionRule getExecutionRule() {
        return executionRule;
    }

    public Pair<AccountIdentifier.Type, AccountIdentifier.Type> getCreditorAndDebtorAccountType() {
        if (Objects.isNull(debtor) || Objects.isNull(debtor.getAccountIdentifier())) {
            return new Pair<>(null, creditor.getAccountIdentifierType());
        }
        return new Pair<>(debtor.getAccountIdentifierType(), creditor.getAccountIdentifierType());
    }

    public boolean isSepa() {
        if (debtor == null) {
            return creditor.getAccountIdentifierType() == Type.IBAN
                    && sepaCountriesWithEur.contains(
                            IbanUtil.getCountryCode(creditor.getAccountNumber()));
        }
        return debtor.getAccountIdentifierType() == Type.IBAN
                && creditor.getAccountIdentifierType() == Type.IBAN
                && sepaCountriesWithEur.contains(IbanUtil.getCountryCode(debtor.getAccountNumber()))
                && sepaCountriesWithEur.contains(
                        IbanUtil.getCountryCode(creditor.getAccountNumber()));
    }

    private String getIbanMarket(String accountNumber) {
        return IbanUtil.getCountryCode(accountNumber);
    }

    public String getMarketCode(
            String accountNumber, String marketCode, AccountIdentifier.Type accountIdentifierType) {
        switch (accountIdentifierType) {
            case PAYM_PHONE_NUMBER:
            case SORT_CODE:
                marketCode = String.valueOf(MarketCode.GB);
                break;
            case IBAN:
                marketCode = getIbanMarket(accountNumber);
                break;
            case SE:
            case BBAN:
                marketCode = String.valueOf(MarketCode.SE);
                break;
            default:
        }
        return marketCode;
    }

    public static class Builder {
        private Creditor creditor;
        private Debtor debtor;
        private Amount amount;
        // TODO rename back to amount after removing `amount` field
        private ExactCurrencyAmount exactCurrencyAmount;
        private LocalDate executionDate;
        private String uniqueId;
        private PaymentStatus status = PaymentStatus.CREATED;
        private PaymentType type = PaymentType.UNDEFINED;
        private String currency;
        /**
         * @deprecated (20200828, remittanceInforation should be used instead, to be removed later)
         */
        @Deprecated private Reference reference;

        private RemittanceInformation remittanceInformation;
        private PaymentScheme paymentScheme;

        private PaymentServiceType paymentServiceType;
        private Frequency frequency;
        private LocalDate startDate;
        private LocalDate endDate;
        private ExecutionRule executionRule;

        public Builder withPaymentServiceType(PaymentServiceType paymentServiceType) {
            this.paymentServiceType = paymentServiceType;
            return this;
        }

        public Builder withFrequency(Frequency frequency) {
            this.frequency = frequency;
            return this;
        }

        public Builder withStartDate(LocalDate startDate) {
            this.startDate = startDate;
            return this;
        }

        public Builder withEndDate(LocalDate endDate) {
            this.endDate = endDate;
            return this;
        }

        public Builder withExecutionRule(ExecutionRule executionRule) {
            this.executionRule = executionRule;
            return this;
        }

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
            this.exactCurrencyAmount =
                    ExactCurrencyAmount.of(amount.doubleValue(), amount.getCurrency());
            return this;
        }

        public Builder withExactCurrencyAmount(ExactCurrencyAmount exactCurrencyAmount) {
            this.amount =
                    new Amount(
                            exactCurrencyAmount.getCurrencyCode(),
                            exactCurrencyAmount.getDoubleValue());
            this.exactCurrencyAmount = exactCurrencyAmount;
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

        // TODO Double Check: This should be removed since we have currency info in
        // exactCurrencyAmount?
        public Builder withCurrency(String currency) {
            this.currency = currency;
            return this;
        }

        /**
         * @deprecated (20200828, remittanceInforation should be used instead, to be removed later)
         */
        @Deprecated
        public Builder withReference(Reference reference) {
            this.reference = reference;
            return this;
        }

        public Builder withPaymentScheme(PaymentScheme paymentScheme) {
            this.paymentScheme = paymentScheme;
            return this;
        }

        public Payment build() {
            return new Payment(this);
        }

        public Builder withRemittanceInformation(RemittanceInformation remittanceInformation) {
            this.remittanceInformation = remittanceInformation;
            return this;
        }
    }
}
