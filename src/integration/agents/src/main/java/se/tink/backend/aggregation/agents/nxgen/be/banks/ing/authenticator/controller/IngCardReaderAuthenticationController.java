package se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.controller;

import com.google.common.base.Preconditions;
import java.util.Objects;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.CredentialsTypes;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.IngCardReaderAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.AuthenticationResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.AuthenticationStepConstants;
import se.tink.backend.aggregation.nxgen.controllers.authentication.LoadedAuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.ProgressiveAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.MultiFactorAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationFormer;
import se.tink.backend.aggregation.nxgen.exceptions.NotImplementedException;

public final class IngCardReaderAuthenticationController
        implements MultiFactorAuthenticator, ProgressiveAuthenticator {

    static final String STEP_OTP = "otp";
    static final String STEP_SIGN = "sign";

    private final IngCardReaderAuthenticator authenticator;
    private final SupplementalInformationFormer supplementalInformationFormer;

    public IngCardReaderAuthenticationController(
            IngCardReaderAuthenticator authenticator,
            SupplementalInformationFormer supplementalInformationFormer) {
        this.authenticator = Preconditions.checkNotNull(authenticator);
        this.supplementalInformationFormer =
                Preconditions.checkNotNull(supplementalInformationFormer);
    }

    @Override
    public AuthenticationResponse authenticate(LoadedAuthenticationRequest authenticationRequest)
            throws AuthenticationException, AuthorizationException {
        Credentials credentials = authenticationRequest.getCredentials();
        NotImplementedException.throwIf(
                !Objects.equals(credentials.getType(), getType()),
                String.format(
                        "Authentication method not implemented for CredentialsType: %s",
                        credentials.getType()));
        switch (authenticationRequest.getStep()) {
            case AuthenticationStepConstants.STEP_INIT:
                return new OtpStep(supplementalInformationFormer).respond();
            case STEP_OTP:
                return new SignStep(supplementalInformationFormer, authenticator)
                        .respond(authenticationRequest);
            case STEP_SIGN:
                return new FinalStep(authenticator).respond(authenticationRequest);
            default:
                throw new IllegalStateException("bad step!");
        }
    }

    @Override
    public CredentialsTypes getType() {
        return CredentialsTypes.PASSWORD;
    }

    // TODO auth: remove when ProgressiveAuthenticator remove extension from Authenticator
    @Override
    public void authenticate(Credentials credentials)
            throws AuthenticationException, AuthorizationException {
        throw new AssertionError();
    }
}
