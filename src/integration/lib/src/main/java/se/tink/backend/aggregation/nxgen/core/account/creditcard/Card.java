package se.tink.backend.aggregation.nxgen.core.account.creditcard;

public class Card {
    private final String cardHolder;
    private final String cardNumber;

    private Card(String cardHolder, String cardNumber) {
        this.cardHolder = cardHolder;
        this.cardNumber = cardNumber;
    }

    public static Card create(String cardHolder, String cardNumber) {
        return new Card(cardHolder, cardNumber);
    }

    public String getCardHolder() {
        return cardHolder;
    }

    public String getCardNumber() {
        return cardNumber;
    }
}
