package se.tink.backend.aggregation.nxgen.core.account.creditcard;

public class CreditCard {
    private final String cardHolder;
    private final String cardNumber;

    private CreditCard(String cardHolder, String cardNumber) {
        this.cardHolder = cardHolder;
        this.cardNumber = cardNumber;
    }

    public static CreditCard of(String cardHolder, String cardNumber) {
        return new CreditCard(cardHolder, cardNumber);
    }

    public String getCardHolder() {
        return cardHolder;
    }

    public String getCardNumber() {
        return cardNumber;
    }
}
