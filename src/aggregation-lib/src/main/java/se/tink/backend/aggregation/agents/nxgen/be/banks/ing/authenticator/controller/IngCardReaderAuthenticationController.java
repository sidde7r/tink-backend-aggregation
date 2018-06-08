package se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.controller;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import java.util.Map;
import java.util.Objects;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.IngCardReaderAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.MultiFactorAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.backend.aggregation.nxgen.exceptions.NotImplementedException;
import se.tink.backend.aggregation.rpc.Credentials;
import se.tink.backend.aggregation.rpc.CredentialsTypes;
import se.tink.backend.aggregation.rpc.Field;
import se.tink.libraries.i18n.Catalog;

public class IngCardReaderAuthenticationController implements MultiFactorAuthenticator {
    private static final String CARD_ID_FIELD = "cardId";
    private static final String OTP_FIELD = "otp";
    private static final String CHALLENGE_RESPONSE_FIELD_KEY = "challengeResponse";

    private final IngCardReaderAuthenticator authenticator;
    private final Catalog catalog;
    private final SupplementalInformationController supplementalInformationController;

    public IngCardReaderAuthenticationController(Catalog catalog,
            SupplementalInformationController supplementalInformationController,
            IngCardReaderAuthenticator authenticator) {
        this.catalog = Preconditions.checkNotNull(catalog);
        this.authenticator = Preconditions.checkNotNull(authenticator);
        this.supplementalInformationController = Preconditions.checkNotNull(supplementalInformationController);
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
        String otp = credentials.getField(OTP_FIELD);

        if (Strings.isNullOrEmpty(username)
                || Strings.isNullOrEmpty(cardNumber)
                || Strings.isNullOrEmpty(otp)) {
            throw LoginError.INCORRECT_CREDENTIALS.exception();
        }

        ChallengeExchangeValues challengeExchangeValues = authenticator.initEnroll(username, cardNumber, otp);

        Map<String, String> supplementalInformation = supplementalInformationController
                .askSupplementalInformation(
                        getChallengeField(challengeExchangeValues.getChallenge()),
                        getChallengeResponseField()
                );

        authenticator.confirmEnroll(
                username,
                supplementalInformation.get(CHALLENGE_RESPONSE_FIELD_KEY),
                challengeExchangeValues.getSigningId()
        );

        authenticator.authenticate(username);
    }

    private Field getChallengeField(String challenge) {
        String helpText = catalog.getString(
                  "1. Insert your bank card into ![](https://Card-Reader_ING@2x.png) the ING Card Reader.\n"
                + "2. Press ![](https://BT_Sign_ING@2x.png)\n"
                + "3. Enter your PIN and press ![](https://BT_OK_ING@2x.png)\n"
                + "4. Enter the following number");

        Field challengeField = new Field();
        challengeField.setDescription(getChallengeFormattedWithSpaces(challenge));
        challengeField.setName("challenge");
        challengeField.setHelpText(helpText);
        challengeField.setValue(getChallengeFormattedWithSpaces(challenge));
        challengeField.setImmutable(true);
        return challengeField;
    }

    private Field getChallengeResponseField() {
        String helpText = catalog.getString(
                  "5. press ![](https://BT_Sign_ING@2x.png.png)\n"
                + "6. Enter the e-signature");
        Field challengeResponse = new Field();
        challengeResponse.setDescription(catalog.getString("Input"));
        challengeResponse.setName(CHALLENGE_RESPONSE_FIELD_KEY);
        challengeResponse.setHelpText(helpText);
        challengeResponse.setNumeric(true);

        return challengeResponse;
    }

    private String getChallengeFormattedWithSpaces(String challenge) {
        if (challenge.length() != 10) {
            // We expect the challenge to consist of 10 numbers, if not we don't try to format for readability
            return challenge;
        }

        return String.format("%s %s %s",
                challenge.substring(0, 4),
                challenge.substring(4, 8),
                challenge.substring(8));
    }
}
