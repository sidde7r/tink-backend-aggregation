package se.tink.libraries.account.identifiers;

import com.google.common.base.Preconditions;
import se.tink.libraries.account.AccountIdentifier;

public class PaymentCardNumberIdentifier extends AccountIdentifier {

    private final String cardNumber;

    public PaymentCardNumberIdentifier(String cardNumber) {
        Preconditions.checkArgument(cardNumber != null, "CardNumber identfier can not be null");

        this.cardNumber = cardNumber;
    }

    @Override
    public String getIdentifier() {
        return cardNumber;
    }

    @Override
    public boolean isValid() {
        // Luhn algorithm used to verify
        int sum = 0;
        boolean alternate = false;
        for (int i = cardNumber.length() - 1; i >= 0; i--) {
            int n = Integer.parseInt(cardNumber.substring(i, i + 1));
            if (alternate) {
                n *= 2;
                if (n > 9) {
                    n = (n % 10) + 1;
                }
            }
            sum += n;
            alternate = !alternate;
        }
        return (sum % 10 == 0);
    }

    @Override
    public Type getType() {
        return Type.PAYMENT_CARD_NUMBER;
    }
}
