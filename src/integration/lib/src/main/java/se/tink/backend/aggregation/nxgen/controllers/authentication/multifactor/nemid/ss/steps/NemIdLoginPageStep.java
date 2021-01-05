package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.steps;

import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.NemIdConstants.HtmlElements.PASSWORD_INPUT;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.NemIdConstants.HtmlElements.SUBMIT_LOGIN_BUTTON;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.NemIdConstants.HtmlElements.USERNAME_INPUT;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.NemIdConstants.NEM_ID_PREFIX;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.NemIdCredentialsStatusUpdater;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.NemIdWebDriverWrapper;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.nemid.NemIdCodeAppConstants.UserMessage;

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
