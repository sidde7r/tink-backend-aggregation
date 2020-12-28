package se.tink.backend.aggregation.nxgen.controllers.authentication.password.dk.nemid.v2.steps;

import static se.tink.backend.aggregation.nxgen.controllers.authentication.password.dk.nemid.v2.NemIdConstantsV2.HtmlElements.PASSWORD_INPUT;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.password.dk.nemid.v2.NemIdConstantsV2.HtmlElements.SUBMIT_LOGIN_BUTTON;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.password.dk.nemid.v2.NemIdConstantsV2.HtmlElements.USERNAME_INPUT;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.password.dk.nemid.v2.NemIdConstantsV2.NEM_ID_PREFIX;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.nemid.NemIdCodeAppConstants.UserMessage;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.dk.nemid.v2.NemIdCredentialsStatusUpdater;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.dk.nemid.v2.NemIdWebDriverWrapper;

@Slf4j
@RequiredArgsConstructor
public class NemIdLoginPageStep {

    private final NemIdWebDriverWrapper driverWrapper;
    private final NemIdCredentialsStatusUpdater statusUpdater;

    public void login(Credentials credentials) {
        setUserName(credentials.getField(Field.Key.USERNAME));
        setPassword(credentials.getField(Field.Key.PASSWORD));
        clickLogin();

        log.info("{} User credentials have been entered", NEM_ID_PREFIX);
        statusUpdater.updateStatusPayload(credentials, UserMessage.VERIFYING_CREDS);
    }

    private void setUserName(String username) {
        driverWrapper.setValueToElement(username, USERNAME_INPUT);
    }

    private void setPassword(String password) {
        driverWrapper.setValueToElement(password, PASSWORD_INPUT);
    }

    private void clickLogin() {
        driverWrapper.clickButton(SUBMIT_LOGIN_BUTTON);
    }
}
