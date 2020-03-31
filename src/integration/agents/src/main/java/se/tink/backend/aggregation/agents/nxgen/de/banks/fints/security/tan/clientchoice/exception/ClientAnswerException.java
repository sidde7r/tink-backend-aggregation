package se.tink.backend.aggregation.agents.nxgen.de.banks.fints.security.tan.clientchoice.exception;

public class ClientAnswerException extends RuntimeException {

    public ClientAnswerException(String s) {
        super(s);
    }

    public ClientAnswerException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public ClientAnswerException(Throwable throwable) {
        super(throwable);
    }
}
