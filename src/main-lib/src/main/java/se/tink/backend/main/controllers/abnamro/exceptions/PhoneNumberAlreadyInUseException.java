package se.tink.backend.main.controllers.abnamro.exceptions;

public class PhoneNumberAlreadyInUseException extends Exception {
    public PhoneNumberAlreadyInUseException(String phoneNumber) {
        super(String.format("Phone number is already in use (PhoneNumber = '%s').", phoneNumber));
    }
}

