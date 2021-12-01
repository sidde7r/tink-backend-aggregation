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
import se.tink.backend.aggregation.nxgen.core.card.Card;
import se.tink.libraries.amount.ExactCurrencyAmount;

public final class CreditCardTransaction extends Transaction {
    private final String creditCardAccountNumber;
    private final Card creditCard;

    private CreditCardTransaction(
            ExactCurrencyAmount amount,
            Date date,
            String description,
            boolean pending,
            String creditCardAccountNumber,
            Card creditCard,
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
        this.creditCardAccountNumber = creditCardAccountNumber;
        this.creditCard = creditCard;
    }

    public static Builder builder() {
        return new Builder();
    }

    public Optional<String> getCreditCardAccountNumber() {
        return Optional.ofNullable(creditCardAccountNumber);
    }

    public Optional<Card> getCreditCard() {
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

        getCreditCardAccountNumber()
                .ifPresent(
                        number ->
                                transaction.setPayload(
                                        TransactionPayloadTypes.SUB_ACCOUNT, number));

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
        private String creditCardAccountNumber;
        private Card creditCard;

        public Builder setCreditCardAccountNumber(String creditCardAccountNumber) {
            this.creditCardAccountNumber = creditCardAccountNumber;
            return this;
        }

        public Builder setCreditCard(Card creditCard) {
            this.creditCard = creditCard;
            return this;
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
                    creditCardAccountNumber,
                    creditCard,
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
