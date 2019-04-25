package se.tink.libraries.account.identifiers;

import com.google.common.base.Preconditions;
import java.util.regex.Pattern;
import se.tink.libraries.account.AccountIdentifier;

/**
 * Paym is a mobile payment system provided by banks and building societies in the United Kingdom.
 * Recipients are identified by their mobile phone number instead of bank details such as sort code
 * and account number.
 *
 * <p>Paym can only be used to pay money into UK current accounts.
 */
public class PaymPhoneNumberIdentifier extends AccountIdentifier {

    private final String phoneNumber;
    private static final Pattern UK_PHONE_NUMBER = Pattern.compile("^[0-9]{10,11}$");

    public PaymPhoneNumberIdentifier(String phoneNumber) {
        Preconditions.checkArgument(phoneNumber != null, "PhoneNumber identfier can not be null");
        this.phoneNumber = phoneNumber;
    }

    @Override
    public String getIdentifier() {
        return phoneNumber;
    }

    @Override
    public boolean isValid() {
        return UK_PHONE_NUMBER.matcher(phoneNumber).matches();
    }

    @Override
    public Type getType() {
        return Type.PAYM_PHONE_NUMBER;
    }
}
