package se.tink.backend.aggregation.nxgen.controllers.payment;

/**
 * Contains constants that are common for the agents that implement PIS, to avoid slight differences
 * in phrasing.
 */
public class PaymentConstants {

    public static final class BankId {
        public static final String CANCELLED =
                "BankID signing of payment was cancelled by the user.";
        public static final String NO_CLIENT = "No BankID client when trying to sign the payment.";
        public static final String TIMEOUT = "BankID signing of payment timed out.";
        public static final String INTERRUPTED = "BankID signing of payment was interrupted.";
        public static final String UNKNOWN = "Unknown problem when signing payment with BankID.";
        public static final String NO_EXTENDED_USE =
                "To add a new recipient, activate Mobile BankID for extended use.";
    }
}
