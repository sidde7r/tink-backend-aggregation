package se.tink.backend.aggregation.agents.nxgen.be.banks.axa.authenticator.errorhandling;

public class ResponseErrorHandlingBuilder {

    public static final ResponseErrorHandler DEFAULT_CHAIN =
            new ResponseErrorHandlingBuilder()
                    .chain(new DetailedAuthenticationErrorHandler())
                    .chain(new AssertionAuthenticationErrorHandler())
                    .chain(new BaseErrorHandler())
                    .build();

    private ResponseErrorHandler first;
    private ResponseErrorHandler current;

    public ResponseErrorHandlingBuilder chain(ResponseErrorHandler next) {
        if (this.first == null) {
            this.first = next;
        }
        if (this.current != null) {
            this.current.nextHandler = next;
        }
        this.current = next;
        return this;
    }

    public ResponseErrorHandler build() {
        return this.first;
    }
}
