package se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.creditcard;

import com.google.common.base.Preconditions;

public final class CreditCardModule {

    private final String cardNumber;
    private final String cardAlias;

    private CreditCardModule(Builder builder) {
        this.cardAlias = builder.cardAlias;
        this.cardNumber = builder.cardNumber;
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

    private static class Builder
            implements CardNumberStep<CardModuleBuildStep>,
                    CardAliasStep<CardModuleBuildStep>,
                    CardModuleBuildStep {
        private String cardNumber;
        private String cardAlias;

        @Override
        public CardAliasStep<CardModuleBuildStep> withCardNumber(String cardNumber) {
            Preconditions.checkNotNull(cardNumber, "Card Number must not be null.");
            String number = cardNumber.replaceAll("\\D+", "");

            Preconditions.checkArgument(
                    number.length() >= 12,
                    "Not enough digits. Should be minimum 12 and most likely 16 digits!");

            this.cardNumber = number;
            return this;
        }

        @Override
        public CardModuleBuildStep withCardAlias(String cardAlias) {
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
