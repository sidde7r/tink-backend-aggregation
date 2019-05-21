package se.tink.libraries.account.identifiers;

import com.google.common.base.Preconditions;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.giro.validation.LuhnCheck;

public class PaymentCardNumberIdentifier extends AccountIdentifier {

    private final String cardNumber;

    public PaymentCardNumberIdentifier(String cardNumber) {
        Preconditions.checkNotNull(cardNumber, "CardNumber identfier can not be null");

        this.cardNumber = cardNumber;
    }

    @Override
    public String getIdentifier() {
        return cardNumber;
    }

    @Override
    public boolean isValid() {
        return LuhnCheck.isLastCharCorrectLuhnMod10Check(cardNumber);
    }

    @Override
    public Type getType() {
        return Type.PAYMENT_CARD_NUMBER;
    }
}
