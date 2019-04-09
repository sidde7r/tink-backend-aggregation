package se.tink.backend.aggregation.agents.creditcards.ikano.api.utils;

public class CreditCardUtils {

    private static final String REPLACE_CHAR = "*";
    private static final String DELIMITER = " ";
    private static final int SHOWN_DIGITS = 4;

    /**
     * Masks credit card numbers to avoid showing sensitive data. For example, "1234123412341234"
     * would become "**** **** **** 1234".
     */
    public static String maskCardNumber(String cardNumber) {
        cardNumber = cardNumber.replaceAll("[- .]", "");
        if (!cardNumber.matches("[0-9]+")) {
            return "";
        } else if (cardNumber.length() <= SHOWN_DIGITS) {
            return cardNumber;
        }

        StringBuilder maskedNumber = new StringBuilder();
        StringBuilder lastDigits = new StringBuilder();
        for (int i = 0; i < cardNumber.length(); i++) {
            if (i < cardNumber.length() - SHOWN_DIGITS) {
                maskedNumber.append(REPLACE_CHAR);
                if ((i + 1) % 4 == 0 && i != 0) {
                    maskedNumber.append(DELIMITER);
                }
            } else {
                lastDigits.append(cardNumber.charAt(i));
            }
        }

        return maskedNumber.toString()
                + (cardNumber.length() % 4 == 0 || cardNumber.length() < 4 ? "" : " ")
                + lastDigits.toString();
    }
}
