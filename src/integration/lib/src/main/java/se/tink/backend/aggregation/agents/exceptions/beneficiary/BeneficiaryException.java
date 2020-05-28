package se.tink.backend.aggregation.agents.exceptions.beneficiary;

public class BeneficiaryException extends Exception {
    public BeneficiaryException(String message) {
        super(message);
    }

    public BeneficiaryException(String message, Throwable cause) {
        super(message, cause);
    }
}
