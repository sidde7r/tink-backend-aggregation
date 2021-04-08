package se.tink.backend.aggregation.nxgen.core.transaction;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import se.tink.backend.aggregation.agents.models.TransactionExternalSystemIdType;
import se.tink.backend.aggregation.agents.models.TransactionPayloadTypes;
import se.tink.backend.aggregation.agents.models.TransactionTypes;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCard;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.libraries.amount.Amount;
import se.tink.libraries.amount.ExactCurrencyAmount;

public final class CreditCardTransaction extends Transaction {
    private final CreditCardAccount creditAccount;
    private final CreditCard creditCard;

    private CreditCardTransaction(
            ExactCurrencyAmount amount,
            Date date,
            String description,
            boolean pending,
            CreditCardAccount creditAccount,
            CreditCard creditCard,
            Map<TransactionExternalSystemIdType, String> externalSystemIds,
            Boolean mutable,
            TransactionDates transactionDates,
            String proprietaryFinancialInstitutionType,
            String merchantName,
            String merchantCategoryCode,
            String transactionReference,
            String providerMarket) {
        super(
                amount,
                date,
                description,
                pending,
                externalSystemIds,
                mutable,
                transactionDates,
                proprietaryFinancialInstitutionType,
                merchantName,
                merchantCategoryCode,
                transactionReference,
                providerMarket);
        this.creditAccount = creditAccount;
        this.creditCard = creditCard;
    }

    public static Builder builder() {
        return new Builder();
    }

    public Optional<CreditCardAccount> getCreditAccount() {
        return Optional.ofNullable(creditAccount);
    }

    public Optional<CreditCard> getCreditCard() {
        return Optional.ofNullable(creditCard);
    }

    @Override
    public TransactionTypes getType() {
        return TransactionTypes.CREDIT_CARD;
    }

    @Override
    public se.tink.backend.aggregation.agents.models.Transaction toSystemTransaction(
            boolean multiCurrencyEnabled) {
        se.tink.backend.aggregation.agents.models.Transaction transaction =
                super.toSystemTransaction(multiCurrencyEnabled);

        getCreditAccount()
                .ifPresent(
                        creditAccount ->
                                transaction.setPayload(
                                        TransactionPayloadTypes.SUB_ACCOUNT,
                                        creditAccount.getAccountNumber()));

        if (Objects.nonNull(creditCard) && Objects.nonNull(creditCard.getCardNumber())) {
            transaction.setPayload(
                    TransactionPayloadTypes.CREDIT_CARD_NUMBER, creditCard.getCardNumber());
        }

        if (Objects.nonNull(creditCard) && Objects.nonNull(creditCard.getCardHolder())) {
            transaction.setPayload(
                    TransactionPayloadTypes.CREDIT_CARD_HOLDER, creditCard.getCardHolder());
        }

        return transaction;
    }

    public static final class Builder extends Transaction.Builder {
        private CreditCardAccount creditAccount;
        private CreditCard creditCard;

        private CreditCardAccount getCreditAccount() {
            return creditAccount;
        }

        public Builder setCreditAccount(CreditCardAccount creditAccount) {
            this.creditAccount = creditAccount;
            return this;
        }

        public CreditCard getCreditCard() {
            return creditCard;
        }

        public Builder setCreditCard(CreditCard creditCard) {
            this.creditCard = creditCard;
            return this;
        }

        @Deprecated
        @Override
        public Builder setAmount(Amount amount) {
            return (Builder) super.setAmount(amount);
        }

        @Override
        public Builder setAmount(ExactCurrencyAmount amount) {
            return (Builder) super.setAmount(amount);
        }

        @Override
        public Builder setDate(Date date) {
            return (Builder) super.setDate(date);
        }

        @Override
        public Builder setDate(LocalDate date) {
            return (Builder) super.setDate(date);
        }

        @Override
        public Builder setDateTime(ZonedDateTime dateTime) {
            return (Builder) super.setDate(dateTime.toLocalDate());
        }

        @Override
        public Builder setDate(CharSequence date, DateTimeFormatter formatter) {
            return (Builder) super.setDate(date, formatter);
        }

        @Override
        public Builder setDescription(String description) {
            return (Builder) super.setDescription(description);
        }

        @Override
        public Builder setPending(boolean pending) {
            return (Builder) super.setPending(pending);
        }

        @Override
        public Builder setRawDetails(Object rawDetails) {
            return (Builder) super.setRawDetails(rawDetails);
        }

        @Override
        public CreditCardTransaction build() {
            return new CreditCardTransaction(
                    getExactAmount(),
                    getDate(),
                    getDescription(),
                    isPending(),
                    getCreditAccount(),
                    getCreditCard(),
                    getExternalSystemIds(),
                    getMutable(),
                    getTransactionDates(),
                    getProprietaryFinancialInstitutionType(),
                    getMerchantName(),
                    getMerchantCategoryCode(),
                    getTransactionReference(),
                    getProviderMarket());
        }
    }
}
