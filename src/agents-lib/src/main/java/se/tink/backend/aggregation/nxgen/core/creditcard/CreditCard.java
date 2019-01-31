package se.tink.backend.aggregation.nxgen.core.creditcard;

public class CreditCard {
    private final String cardNumber;

    private CreditCard(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public static CreditCard create(String cardNumber) {
        return new CreditCard(cardNumber);
    }

    public String getCardNumber() {
        return cardNumber;
    }
}
