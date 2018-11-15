package se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator;

import com.google.common.base.Strings;
import org.apache.commons.lang3.StringUtils;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.SupplementalInfoException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.FortisApiClient;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.FortisConstants;
import se.tink.backend.aggregation.agents.utils.encoding.EncodingUtils;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.MultiFactorAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.rpc.Credentials;
import se.tink.backend.aggregation.rpc.CredentialsTypes;
import se.tink.backend.aggregation.rpc.Field;
import se.tink.libraries.i18n.Catalog;
import se.tink.libraries.i18n.LocalizableKey;

public class FortisAuthenticator implements MultiFactorAuthenticator, AutoAuthenticator {

    private final Catalog catalog;
    private final PersistentStorage persistentStorage;
    private final FortisApiClient apiClient;
    private final SupplementalInformationController supplementalInformationController;

    public FortisAuthenticator(Catalog catalog, PersistentStorage persistentStorage, FortisApiClient apiClient,
            SupplementalInformationController supplementalInformationController) {
        this.catalog = catalog;
        this.persistentStorage = persistentStorage;
        this.apiClient = apiClient;
        this.supplementalInformationController = supplementalInformationController;
    }

    @Override
    public CredentialsTypes getType() {
        return CredentialsTypes.PASSWORD;
    }

    @Override
    public void authenticate(Credentials credentials) throws AuthenticationException, AuthorizationException {

    }

    @Override
    public void autoAuthenticate() throws SessionException {

    }

    private String waitForLoginCode(String challenge) throws SupplementalInfoException {
    return waitForSupplementalInformation(
        createDescriptionField(catalog.getString(
                "1$ ![](https://easybanking.bnpparibasfortis.be/rsc/serv/bank/KBC/KBC_CardReader.png)Insert your bank card into the card reader\n"
                + "2$ ![](https://easybanking.bnpparibasfortis.be/rsc/serv/bank/KBC/KBC_LOGIN.png) ![](https://easybanking.bnpparibasfortis.be/rsc/serv/bank/KBC/KBC_LOGIN.png) Tap\n"
                + "3$  Enter the start code []"),
            challenge),
        createInputField(catalog.getString(
                "4$ ![](https://easybanking.bnpparibasfortis.be/rsc/serv/bank/KBC/KBC_OK.png) Tap\n"
                    + "5$  Enter your secret code\n"
                    + "6$ ![](https://easybanking.bnpparibasfortis.be/rsc/serv/bank/KBC/KBC_OK.png) Tap\n"
                    + "7$  Enter the login code")));
    }

    private String waitForSignCode(String challenge) throws SupplementalInfoException {
    return waitForSupplementalInformation(
        createDescriptionField(catalog.getString(
            "1$ ![](https://easybanking.bnpparibasfortis.be/rsc/serv/bank/KBC/KBC_CardReader.png) Insert your bank card into the card reader\n"
                + "2$ ![](https://easybanking.bnpparibasfortis.be/rsc/serv/bank/KBC/KBC_SIGN.png) ![](https://easybanking.bnpparibasfortis.be/rsc/serv/bank/KBC/KBC_SIGN.png) Tap\n"
                + "3$  Enter the start code []"),
            challenge),
        createInputField(catalog.getString(
            "4$ ![](https://easybanking.bnpparibasfortis.be/rsc/serv/bank/KBC/KBC_OK.png) ![](https://easybanking.bnpparibasfortis.be/rsc/serv/bank/KBC/KBC_OK.png) Tap\n"
                    + "5$  Enter your PIN\n"
                    + "6$ ![](https://easybanking.bnpparibasfortis.be/rsc/serv/bank/KBC/KBC_OK.png) Tap\n"
                    + "7$  Enter the sign code")));
    }

    private String waitForSupplementalInformation(Field... fields)
            throws SupplementalInfoException {
        return supplementalInformationController.askSupplementalInformation(fields)
                .get(FortisConstants.MultiFactorAuthentication.CODE);
    }

    private Field createDescriptionField(String helpText, String challenge) {
        String formattedChallenge = getChallengeFormattedWithSpace(challenge);
        Field field = new Field();
        field.setMasked(false);
        field.setDescription(formattedChallenge);
        field.setValue(formattedChallenge);
        field.setName("description");
        field.setHelpText(helpText);
        field.setImmutable(true);
        return field;
    }

    private Field createInputField(String helpText) {
        Field field = new Field();
        field.setMasked(false);
        field.setDescription(catalog.getString("Input"));
        field.setName(FortisConstants.MultiFactorAuthentication.CODE);
        field.setHelpText(helpText);
        field.setNumeric(true);
        field.setHint("NNNNNNN");
        return field;
    }

    private String getChallengeFormattedWithSpace(String challenge) {
        if (challenge.length() != 8) {
            // We expect the challenge to consist of 8 numbers, if not we don't try to format for readability
            return challenge;
        }

        return String.format("%s %s",
                challenge.substring(0, 4),
                challenge.substring(4));
    }
}
