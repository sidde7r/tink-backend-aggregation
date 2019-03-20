package se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.controller;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.CredentialsTypes;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.IngCardReaderAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.AuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.AuthenticationResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.AuthenticationStepConstants;
import se.tink.backend.aggregation.nxgen.controllers.authentication.ProgressiveAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.MultiFactorAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationFormer;
import se.tink.backend.aggregation.nxgen.exceptions.NotImplementedException;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class IngCardReaderAuthenticationController implements MultiFactorAuthenticator, ProgressiveAuthenticator {
    private static final String CARD_ID_FIELD = "cardId";

    private static final String STEP_OTP = "otp";
    private static final String STEP_SIGN = "sign";

    private static final String SIGN_ID = "signId";

    private final IngCardReaderAuthenticator authenticator;
    private final SupplementalInformationFormer supplementalInformationFormer;

    public IngCardReaderAuthenticationController(
            IngCardReaderAuthenticator authenticator,
            SupplementalInformationFormer supplementalInformationFormer) {
        this.authenticator = Preconditions.checkNotNull(authenticator);
        this.supplementalInformationFormer = Preconditions.checkNotNull(supplementalInformationFormer);
    }

    @Override
    public AuthenticationResponse authenticate(AuthenticationRequest authenticationRequest)
            throws AuthenticationException, AuthorizationException {
        Credentials credentials = authenticationRequest.getCredentials();
        NotImplementedException.throwIf(
                !Objects.equals(credentials.getType(), getType()),
                String.format(
                        "Authentication method not implemented for CredentialsType: %s",
                        credentials.getType()));
        switch (authenticationRequest.getStep()) {
            case AuthenticationStepConstants.STEP_INIT:
                List<Field> otpInput =
                        Collections.singletonList(
                                supplementalInformationFormer.getField(Field.Key.OTP_INPUT));
                return new AuthenticationResponse(STEP_OTP, otpInput);
            case STEP_OTP:
                String username = credentials.getField(Field.Key.USERNAME);
                String cardNumber = credentials.getField(CARD_ID_FIELD);
                String otp = authenticationRequest.getUserInputs().get(0);
                if (Strings.isNullOrEmpty(username)
                        || Strings.isNullOrEmpty(cardNumber)
                        || Strings.isNullOrEmpty(otp)) {
                    throw LoginError.INCORRECT_CREDENTIALS.exception();
                }
                ChallengeExchangeValues challengeExchangeValues = authenticator.initEnroll(username, cardNumber, otp);
                credentials.setSensitivePayload(SIGN_ID, challengeExchangeValues.getSigningId());
                return new AuthenticationResponse(
                        STEP_SIGN,
                        supplementalInformationFormer.formChallenageResponseFields(
                                challengeExchangeValues.getChallenge()));
            case STEP_SIGN:
                authenticator.confirmEnroll(
                        credentials.getField(Field.Key.USERNAME),
                        authenticationRequest.getUserInputs().get(1),
                        credentials.getSensitivePayload(SIGN_ID));
                authenticator.authenticate(credentials.getField(Field.Key.USERNAME));
                return new AuthenticationResponse(AuthenticationStepConstants.STEP_FINALIZE, null);
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
    public void authenticate(Credentials credentials) throws AuthenticationException, AuthorizationException {
        throw new AssertionError();
    }
}
