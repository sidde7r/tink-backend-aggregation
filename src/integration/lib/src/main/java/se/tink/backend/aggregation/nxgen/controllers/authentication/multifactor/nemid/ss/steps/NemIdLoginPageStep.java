package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.steps;

import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.NemIdConstants.HtmlElements.PASSWORD_INPUT;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.NemIdConstants.HtmlElements.SUBMIT_BUTTON;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.NemIdConstants.HtmlElements.USERNAME_INPUT;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.NemIdConstants.NEM_ID_PREFIX;

import com.google.inject.Inject;
import java.util.Base64;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.NemIdCredentialsStatusUpdater;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.utils.NemIdWebDriverWrapper;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.nemid.NemIdCodeAppConstants.UserMessage;
import se.tink.libraries.cryptography.hash.Hash;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class NemIdLoginPageStep {

    private static final Base64.Encoder BASE64_ENCODER = Base64.getEncoder();
    private static final String STATIC_SALT = "9hR2;Rm[j+Lypwt]gymg";

    private final NemIdWebDriverWrapper driverWrapper;
    private final NemIdCredentialsStatusUpdater statusUpdater;

    public void login(Credentials credentials) {
        logCredentialsHashes(credentials);

        setUserName(credentials.getField(Field.Key.USERNAME));
        setPassword(credentials.getField(Field.Key.PASSWORD));
        clickLogin();

        log.info("{} User credentials have been entered", NEM_ID_PREFIX);
        statusUpdater.updateStatusPayload(credentials, UserMessage.VERIFYING_CREDS);
    }

    private void logCredentialsHashes(Credentials credentials) {
        /*
        ITE-3028
        There seems to be an issue that user credentials remain the same even after user changes their
        NemID password. To fix that, we added cleaning of data stored in credentials. These logs might help with
        checking if the issue is resolved.
         */
        log.info(
                "{} Hashes: {}, {}",
                NEM_ID_PREFIX,
                BASE64_ENCODER
                        .encodeToString(
                                Hash.sha512(credentials.getField(Field.Key.USERNAME) + STATIC_SALT))
                        .substring(0, 6),
                BASE64_ENCODER
                        .encodeToString(
                                Hash.sha512(credentials.getField(Field.Key.PASSWORD) + STATIC_SALT))
                        .substring(0, 6));
    }

    private void setUserName(String username) {
        driverWrapper.setValueToElement(username, USERNAME_INPUT);
    }

    private void setPassword(String password) {
        driverWrapper.setValueToElement(password, PASSWORD_INPUT);
    }

    private void clickLogin() {
        driverWrapper.clickButton(SUBMIT_BUTTON);
    }
}
