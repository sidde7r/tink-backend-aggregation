package se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.controller;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import java.util.Objects;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.IngCardReaderAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.MultiFactorAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.exceptions.NotImplementedException;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.CredentialsTypes;

public class IngCardReaderAuthenticationController implements MultiFactorAuthenticator {
    private static final String CARD_ID_FIELD = "cardId";

    private final IngCardReaderAuthenticator authenticator;
    private final SupplementalInformationHelper supplementalInformationHelper;

    public IngCardReaderAuthenticationController(
            IngCardReaderAuthenticator authenticator,
            SupplementalInformationHelper supplementalInformationHelper) {
        this.authenticator = Preconditions.checkNotNull(authenticator);
        this.supplementalInformationHelper = Preconditions.checkNotNull(supplementalInformationHelper);
    }

    @Override
    public CredentialsTypes getType() {
        return CredentialsTypes.PASSWORD;
    }

    @Override
    public void authenticate(Credentials credentials) throws AuthenticationException, AuthorizationException {
        NotImplementedException.throwIf(!Objects.equals(credentials.getType(), getType()),
                String.format("Authentication method not implemented for CredentialsType: %s", credentials.getType()));

        String username = credentials.getField(Field.Key.USERNAME);
        String cardNumber = credentials.getField(CARD_ID_FIELD);

        String otp = supplementalInformationHelper.waitForOtpInput();

        if (Strings.isNullOrEmpty(username) || Strings.isNullOrEmpty(cardNumber) || Strings.isNullOrEmpty(otp)) {
            throw LoginError.INCORRECT_CREDENTIALS.exception();
        }

        ChallengeExchangeValues challengeExchangeValues = authenticator.initEnroll(username, cardNumber, otp);

        String response = supplementalInformationHelper.waitForSignCodeChallengeResponse(
                challengeExchangeValues.getChallenge());

        authenticator.confirmEnroll(username, response, challengeExchangeValues.getSigningId());
        authenticator.authenticate(username);
    }
}
