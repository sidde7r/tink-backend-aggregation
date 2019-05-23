package se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.controller;

import com.google.api.client.repackaged.com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.IngCardReaderAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.AuthenticationResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.LoadedAuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationFormer;

public final class SignStep {

    private final SupplementalInformationFormer supplementalInformationFormer;
    private final IngCardReaderAuthenticator authenticator;

    private static final String CARD_ID_FIELD = "cardId";
    private static final String SIGN_ID = "signId";

    private static Logger logger = LoggerFactory.getLogger(SignStep.class);

    SignStep(
            final SupplementalInformationFormer supplementalInformationFormer,
            final IngCardReaderAuthenticator authenticator) {
        this.supplementalInformationFormer = supplementalInformationFormer;
        this.authenticator = authenticator;
    }

    public AuthenticationResponse respond(LoadedAuthenticationRequest request)
            throws AuthenticationException, AuthorizationException {
        logger.info("ING step2: {}", request.getUserInputs());

        String username = request.getCredentials().getField(Field.Key.USERNAME);
        String cardNumber = request.getCredentials().getField(CARD_ID_FIELD);
        String otp = request.getUserInputs().get(0);
        if (Strings.isNullOrEmpty(username)
                || Strings.isNullOrEmpty(cardNumber)
                || Strings.isNullOrEmpty(otp)) {
            throw LoginError.INCORRECT_CREDENTIALS.exception();
        }
        ChallengeExchangeValues challengeExchangeValues =
                authenticator.initEnroll(username, cardNumber, otp);
        request.getCredentials()
                .setSensitivePayload(SIGN_ID, challengeExchangeValues.getSigningId());
        return new AuthenticationResponse(
                IngCardReaderAuthenticationController.STEP_SIGN,
                supplementalInformationFormer.formChallengeResponseFields(
                        challengeExchangeValues.getChallenge()));
    }
}
