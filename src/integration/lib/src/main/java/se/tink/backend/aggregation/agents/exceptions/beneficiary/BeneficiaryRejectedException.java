package se.tink.backend.aggregation.agents.exceptions.beneficiary;

public class BeneficiaryRejectedException extends BeneficiaryException {
    private static final String DEFAULT_MESSAGE = "Beneficiary request was rejected";
    private static final String DEFAULT_MESSAGE_FORMAT =
            "Beneficiary request was rejected due to: %s";

    public BeneficiaryRejectedException() {
        super(DEFAULT_MESSAGE);
    }

    public BeneficiaryRejectedException(String message, Throwable cause) {
        super(message, cause);
    }

    public BeneficiaryRejectedException(String message) {
        super(message);
    }

    public static BeneficiaryRejectedException createWithReasonMessage(String reason) {
        return new BeneficiaryRejectedException(String.format(DEFAULT_MESSAGE_FORMAT, reason));
    }
}
