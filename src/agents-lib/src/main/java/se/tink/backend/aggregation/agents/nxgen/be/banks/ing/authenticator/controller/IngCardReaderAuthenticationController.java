package se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.controller;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import java.util.Map;
import java.util.Objects;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.IngConstants;
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

        Map<String, String> supplementalInformation = supplementalInformationController.askSupplementalInformation(getOTPField());
        String otp = supplementalInformation.get(OTP_FIELD);

        if (Strings.isNullOrEmpty(username)
                || Strings.isNullOrEmpty(cardNumber)
                || Strings.isNullOrEmpty(otp)) {
            throw LoginError.INCORRECT_CREDENTIALS.exception();
        }

        ChallengeExchangeValues challengeExchangeValues = authenticator.initEnroll(username, cardNumber, otp);

        supplementalInformation = supplementalInformationController
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

    private Field getOTPField(){
     Field otpField = new Field();
     otpField.setDescription("Response code");
     otpField.setName(OTP_FIELD);
     otpField.setNumeric(true);
     otpField.setSensitive(true);
     otpField.setHelpText(catalog.getString("1$  ![](https://easybanking.bnpparibasfortis.be/rsc/serv/bank/ING/ING_CardReader.png) Insert your ING bank card into the ING Card Reader\n"
             + "2$ ![](https://easybanking.bnpparibasfortis.be/rsc/serv/bank/ING/ING_IDENTIFY.png)\n"
             + "3$  Enter your PIN\n"
             + "4$ ![](https://easybanking.bnpparibasfortis.be/rsc/serv/bank/ING/ING_OK.png) Press\n"
             + "5$ Enter the response code"));
     return otpField;
    }

    private Field getChallengeField(String challenge) {
    String helpText =
        catalog.getString(
            "1$  ![](https://easybanking.bnpparibasfortis.be/rsc/serv/bank/ING/ING_CardReader.png) Insert your ING bank card in your ING Card Reader\n"
                    + "2$  Press ![](https://easybanking.bnpparibasfortis.be/rsc/serv/bank/ING/ING_SIGN.png) Press\n"
                    + "3$ ![](https://easybanking.bnpparibasfortis.be/rsc/serv/bank/ING/ING_OK.png) Enter your PIN and press\n"
                    + "4$  Enter the following number []");

        Field challengeField = new Field();
        challengeField.setDescription(getChallengeFormattedWithSpaces(challenge));
        challengeField.setName("challenge");
        challengeField.setHelpText(helpText);
        challengeField.setValue(getChallengeFormattedWithSpaces(challenge));
        challengeField.setImmutable(true);
        return challengeField;
    }

    private Field getChallengeResponseField() {
    String helpText =
        catalog.getString(
            "5$ ![](https://easybanking.bnpparibasfortis.be/rsc/serv/bank/ING/ING_SIGN.png) And press\n"
                    + "6$ Enter your RESPONSE code");
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
