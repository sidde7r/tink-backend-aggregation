package se.tink.libraries.phonenumbers;

public class InvalidPhoneNumberException extends Exception {
    public InvalidPhoneNumberException(String phoneNumber) {
        super(String.format("Phone number is not valid: %s", phoneNumber));
    }
}
