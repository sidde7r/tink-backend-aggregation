package se.tink.backend.aggregation.agents.nxgen.be.openbanking.argenta;

public class ArgentaCertificateException extends RuntimeException {
    public ArgentaCertificateException(String message) {
        super(message);
    }

    public ArgentaCertificateException(String message, Throwable cause) {
        super(message, cause);
    }
}
