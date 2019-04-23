package se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.controller;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

public class IngCardReaderAuthenticationController
        implements MultiFactorAuthenticator, ProgressiveAuthenticator {
    private static Logger logger =
            LoggerFactory.getLogger(IngCardReaderAuthenticationController.class);

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
        this.supplementalInformationFormer =
                Preconditions.checkNotNull(supplementalInformationFormer);
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
                return step1();
            case STEP_OTP:
                return step2(authenticationRequest);
            case STEP_SIGN:
                return step3(authenticationRequest);
            default:
                throw new IllegalStateException("bad step!");
        }
    }

    private AuthenticationResponse step1() {
        List<Field> otpInput =
                Collections.singletonList(
                        supplementalInformationFormer.getField(Field.Key.OTP_INPUT));
        return new AuthenticationResponse(STEP_OTP, otpInput);
    }

    private AuthenticationResponse step2(AuthenticationRequest authenticationRequest)
            throws AuthenticationException, AuthorizationException {
        logger.info("ING step2: {}", authenticationRequest.getUserInputs());

        String username = authenticationRequest.getCredentials().getField(Field.Key.USERNAME);
        String cardNumber = authenticationRequest.getCredentials().getField(CARD_ID_FIELD);
        String otp = authenticationRequest.getUserInputs().get(0);
        if (Strings.isNullOrEmpty(username)
                || Strings.isNullOrEmpty(cardNumber)
                || Strings.isNullOrEmpty(otp)) {
            throw LoginError.INCORRECT_CREDENTIALS.exception();
        }
        ChallengeExchangeValues challengeExchangeValues =
                authenticator.initEnroll(username, cardNumber, otp);
        authenticationRequest
                .getCredentials()
                .setSensitivePayload(SIGN_ID, challengeExchangeValues.getSigningId());
        return new AuthenticationResponse(
                STEP_SIGN,
                supplementalInformationFormer.formChallengeResponseFields(
                        challengeExchangeValues.getChallenge()));
    }

    private AuthenticationResponse step3(AuthenticationRequest authenticationRequest)
            throws AuthenticationException {
        logger.info("ING step3: {}", authenticationRequest.getUserInputs());

        authenticator.confirmEnroll(
                authenticationRequest.getCredentials().getField(Field.Key.USERNAME),
                authenticationRequest.getUserInputs().get(1),
                authenticationRequest.getCredentials().getSensitivePayload(SIGN_ID));
        authenticator.authenticate(
                authenticationRequest.getCredentials().getField(Field.Key.USERNAME));
        return new AuthenticationResponse(AuthenticationStepConstants.STEP_FINALIZE, null);
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
