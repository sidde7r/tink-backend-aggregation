package se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.creditcard;

import com.google.common.base.Preconditions;
import javax.annotation.Nonnull;
import se.tink.libraries.amount.ExactCurrencyAmount;

public final class CreditCardModule {

    private final String cardNumber;
    private final String cardAlias;
    private final ExactCurrencyAmount balance;
    private final ExactCurrencyAmount availableCredit;

    private CreditCardModule(Builder builder) {
        this.cardAlias = builder.cardAlias;
        this.cardNumber = builder.cardNumber;
        this.balance = builder.balance;
        this.availableCredit = builder.availableCredit;
    }

    public static CardNumberStep<CardModuleBuildStep> builder() {
        return new Builder();
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public String getCardAlias() {
        return cardAlias;
    }

    public ExactCurrencyAmount getBalance() {
        return balance;
    }

    public ExactCurrencyAmount getAvailableCredit() {
        return availableCredit;
    }

    private static class Builder
            implements CardNumberStep<CardModuleBuildStep>,
                    CardBalanceStep<CardModuleBuildStep>,
                    CardCreditStep<CardModuleBuildStep>,
                    CardAliasStep<CardModuleBuildStep>,
                    CardModuleBuildStep {
        private String cardNumber;
        private String cardAlias;
        private ExactCurrencyAmount balance;
        private ExactCurrencyAmount availableCredit;

        @Override
        public CardBalanceStep<CardModuleBuildStep> withCardNumber(@Nonnull String cardNumber) {
            Preconditions.checkNotNull(cardNumber, "Card Number must not be null.");
            final String trimmedNumber = cardNumber.replaceAll("\\D+", "");

            Preconditions.checkArgument(
                    trimmedNumber.length() >= 4, "Card number must be at least four digits.");

            this.cardNumber = cardNumber;
            return this;
        }

        @Override
        public CardCreditStep<CardModuleBuildStep> withBalance(
                @Nonnull ExactCurrencyAmount balance) {
            Preconditions.checkNotNull(balance, "Balance must not be null.");
            this.balance = balance;
            return this;
        }

        @Override
        public CardAliasStep<CardModuleBuildStep> withAvailableCredit(
                @Nonnull ExactCurrencyAmount availableCredit) {
            Preconditions.checkNotNull(availableCredit, "Available Credit must not be null.");
            this.availableCredit = availableCredit;
            return this;
        }

        @Override
        public CardModuleBuildStep withCardAlias(@Nonnull String cardAlias) {
            Preconditions.checkNotNull(cardAlias, "Card Alias must not be null");
            this.cardAlias = cardAlias;
            return this;
        }

        @Override
        public CreditCardModule build() {
            return new CreditCardModule(this);
        }
    }
}
