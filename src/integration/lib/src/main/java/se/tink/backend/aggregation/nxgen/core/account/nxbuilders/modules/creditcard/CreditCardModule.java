package se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.creditcard;

import com.google.common.base.Preconditions;
import java.time.Instant;
import java.util.Map;
import javax.annotation.Nonnull;
import org.apache.commons.lang3.tuple.Pair;
import se.tink.backend.agents.rpc.AccountBalanceType;
import se.tink.libraries.amount.ExactCurrencyAmount;

public final class CreditCardModule {

    private final String cardNumber;
    private final String cardAlias;
    private final ExactCurrencyAmount balance;
    private final ExactCurrencyAmount availableCredit;
    private final Map<AccountBalanceType, Pair<ExactCurrencyAmount, Instant>>
            granularAccountBalances;

    private CreditCardModule(Builder builder) {
        this.cardAlias = builder.cardAlias;
        this.cardNumber = builder.cardNumber;
        this.balance = builder.balance;
        this.availableCredit = builder.availableCredit;
        this.granularAccountBalances = builder.granularAccountBalances;
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

    public Map<AccountBalanceType, Pair<ExactCurrencyAmount, Instant>>
            getGranularAccountBalances() {
        return granularAccountBalances;
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
        private Map<AccountBalanceType, Pair<ExactCurrencyAmount, Instant>> granularAccountBalances;

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
        public CardCreditStep<CardModuleBuildStep> withGranularBalances(
                @Nonnull
                        Map<AccountBalanceType, Pair<ExactCurrencyAmount, Instant>>
                                granularAccountBalances) {
            Preconditions.checkNotNull(
                    granularAccountBalances, "Granular balance must not be null.");
            this.granularAccountBalances = granularAccountBalances;
            return this;
        }

        @Override
        public CardCreditStep<CardModuleBuildStep> withBalanceAndGranularBalances(
                @Nonnull ExactCurrencyAmount balance,
                @Nonnull
                        Map<AccountBalanceType, Pair<ExactCurrencyAmount, Instant>>
                                granularAccountBalances) {
            Preconditions.checkNotNull(balance, "Balance must not be null.");
            Preconditions.checkNotNull(
                    granularAccountBalances, "Granular balance must not be null.");
            this.balance = balance;
            this.granularAccountBalances = granularAccountBalances;
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
