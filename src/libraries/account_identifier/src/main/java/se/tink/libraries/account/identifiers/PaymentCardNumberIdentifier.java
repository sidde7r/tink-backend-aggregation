package se.tink.libraries.account.identifiers;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import org.apache.commons.lang3.StringUtils;
import se.tink.libraries.account.AccountIdentifier;

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
        return !Strings.isNullOrEmpty(cardNumber) && StringUtils.isNumeric(cardNumber);
    }

    @Override
    public Type getType() {
        return Type.PAYMENT_CARD_NUMBER;
    }
}
