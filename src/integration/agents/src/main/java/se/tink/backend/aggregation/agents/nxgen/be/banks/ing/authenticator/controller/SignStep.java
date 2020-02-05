package se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.controller;

import com.google.common.base.Strings;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.agents.rpc.Field.Key;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.IngCardReaderAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.AuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.AuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.AuthenticationStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.SupplementInformationRequester;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationFormer;

public final class SignStep implements AuthenticationStep {

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

    @Override
    public AuthenticationStepResponse execute(final AuthenticationRequest request)
            throws AuthenticationException, AuthorizationException {
        logger.info("ING SignStep: {}", request.getUserInputsAsList());

        String username = request.getCredentials().getField(Field.Key.USERNAME);
        String cardNumber = request.getCredentials().getField(CARD_ID_FIELD);
        String otp = request.getUserInputsAsList().get(0);
        if (Strings.isNullOrEmpty(username)
                || Strings.isNullOrEmpty(cardNumber)
                || Strings.isNullOrEmpty(otp)) {
            throw LoginError.INCORRECT_CREDENTIALS.exception();
        }
        ChallengeExchangeValues challengeExchangeValues =
                authenticator.initEnroll(username, cardNumber, otp);
        request.getCredentials()
                .setSensitivePayload(SIGN_ID, challengeExchangeValues.getSigningId());
        List<Field> fields =
                supplementalInformationFormer.formChallengeResponseFields(
                        Key.SIGN_CODE_DESCRIPTION,
                        Key.SIGN_CODE_INPUT,
                        challengeExchangeValues.getChallenge());
        return AuthenticationStepResponse.requestForSupplementInformation(
                new SupplementInformationRequester.Builder().withFields(fields).build());
    }
}
