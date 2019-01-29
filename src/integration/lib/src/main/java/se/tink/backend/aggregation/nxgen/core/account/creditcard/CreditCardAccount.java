package se.tink.backend.aggregation.nxgen.core.account.creditcard;

import com.google.common.base.Preconditions;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.nxgen.core.account.Account;
import se.tink.libraries.amount.Amount;

import java.util.Objects;

public class CreditCardAccount extends Account {

    private CreditCardAccount(Builder<CreditCardAccount, DefaultCreditCardBuilder> builder) {
        super(builder);
    }

    public static Builder<?, ?> builder(String uniqueIdentifier) {
        return new DefaultCreditCardBuilder(uniqueIdentifier);
    }

    public static Builder<?, ?> builder(
            String uniqueIdentifier, Amount balance, Amount availableCredit) {
        return builder(uniqueIdentifier).setBalance(balance).setAvailableCredit(availableCredit);
    }

    /**
     * Variation of {@link #builderFromFullNumber(String, String)} for when there is no suitable
     * card alias.
     */
    public static Builder<?, ?> builderFromFullNumber(String creditCardNumber) {
        Objects.requireNonNull(creditCardNumber);
        return makeBuilder(creditCardNumber, null);
    }

    /**
     * This is the preferred factory method when you have the complete credit card number. It
     * defines the uniqueIdentifier, accountNumber and name in a standard way.
     *
     * @param creditCardNumber Complete credit card number (any/all formatting, or non-digits, will
     *     be removed)
     * @param cardAlias A (user defined) card name
     * @return A builder with the uniqueIdentifier and accountNumber defined in a standard way
     */
    public static Builder<?, ?> builderFromFullNumber(String creditCardNumber, String cardAlias) {
        Objects.requireNonNull(creditCardNumber);
        Objects.requireNonNull(cardAlias);
        return makeBuilder(creditCardNumber, cardAlias);
    }

    private static Builder<?, ?> makeBuilder(String cardNumber, String cardAlias) {

        String unformatted = cardNumber.replaceAll("\\D+", "");

        Preconditions.checkState(
                unformatted.length() >= 12,
                "Not enough digits. Should be minimum 12 and most likely 16 digits!");

        String firstFour = unformatted.substring(0, 4);
        String lastFour = unformatted.substring(unformatted.length() - 4);

        String uniqueIdentifier = String.format("%s%s", firstFour, lastFour);
        String accountNumber = String.format("%s **** **** %s", firstFour, lastFour);
        String name =
                cardAlias != null ? String.format("%s *%s", cardAlias, lastFour) : accountNumber;

        return new DefaultCreditCardBuilder(uniqueIdentifier)
                .setAccountNumber(accountNumber)
                .setName(name);
    }

    @Override
    public AccountTypes getType() {
        return AccountTypes.CREDIT_CARD;
    }

    public abstract static class Builder<
            A extends CreditCardAccount, T extends CreditCardAccount.Builder<A, T>>
            extends Account.Builder<A, T> {

        public Builder(String uniqueIdentifier) {
            super(uniqueIdentifier);
        }
    }

    private static class DefaultCreditCardBuilder
            extends CreditCardAccount.Builder<CreditCardAccount, DefaultCreditCardBuilder> {

        public DefaultCreditCardBuilder(String uniqueIdentifier) {
            super(uniqueIdentifier);
        }

        @Override
        protected DefaultCreditCardBuilder self() {
            return this;
        }

        @Override
        public CreditCardAccount build() {
            return new CreditCardAccount(self());
        }
    }
}
