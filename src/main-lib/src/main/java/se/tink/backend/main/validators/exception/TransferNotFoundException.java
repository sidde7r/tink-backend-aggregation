package se.tink.backend.main.validators.exception;

public class TransferNotFoundException extends RuntimeException {
    public static String MESSAGE = "Transfer not found";

    public TransferNotFoundException(){
        super(TransferNotFoundException.MESSAGE);
    }
}
