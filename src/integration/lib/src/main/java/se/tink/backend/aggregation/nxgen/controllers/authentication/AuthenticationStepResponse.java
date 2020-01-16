package se.tink.backend.aggregation.nxgen.controllers.authentication;

import java.util.Optional;

public class AuthenticationStepResponse {

    private SupplementInformationRequester supplementInformationRequester;
    private String nextStepId;
    private boolean authenticationFinished;

    private AuthenticationStepResponse(
            final SupplementInformationRequester supplementInformationRequester) {
        this.supplementInformationRequester = supplementInformationRequester;
    }

    private AuthenticationStepResponse(String nextStepId) {
        this.nextStepId = nextStepId;
    }

    private AuthenticationStepResponse(boolean authenticationFinished) {
        this.authenticationFinished = authenticationFinished;
    }

    private AuthenticationStepResponse() {}

    public static AuthenticationStepResponse requestForSupplementInformation(
            final SupplementInformationRequester supplementInformationRequester) {
        return new AuthenticationStepResponse((supplementInformationRequester));
    }

    public static AuthenticationStepResponse executeStepWithId(String nextStepId) {
        return new AuthenticationStepResponse(nextStepId);
    }

    public static AuthenticationStepResponse authenticationSucceeded() {
        return new AuthenticationStepResponse(true);
    }

    public static AuthenticationStepResponse executeNextStep() {
        return new AuthenticationStepResponse();
    }

    public Optional<SupplementInformationRequester> getSupplementInformationRequester() {
        return Optional.ofNullable(supplementInformationRequester);
    }

    public Optional<String> getNextStepId() {
        return Optional.ofNullable(nextStepId);
    }

    public boolean isAuthenticationFinished() {
        return authenticationFinished;
    }
}
