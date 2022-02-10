package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.steps.loginpage;

import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.NemIdConstants.HtmlElements.PASSWORD_INPUT;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.NemIdConstants.HtmlElements.SUBMIT_BUTTON;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.NemIdConstants.HtmlElements.USERNAME_INPUT;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.NemIdConstants.NEM_ID_PREFIX;

import com.google.inject.Inject;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.NemIdCredentials;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.NemIdCredentialsProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.NemIdCredentialsStatusUpdater;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.utils.NemIdWebDriverWrapper;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.nemid.NemIdCodeAppConstants.UserMessage;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.libraries.i18n_aggregation.Catalog;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class NemIdLoginPageStep {

    private final NemIdWebDriverWrapper driverWrapper;
    private final NemIdCredentialsStatusUpdater statusUpdater;
    private final NemIdCredentialsProvider credentialsProvider;
    private final Catalog catalog;
    private final SupplementalInformationController supplementalInformationController;

    public void login(Credentials credentials) {
        NemIdCredentials nemIdCredentials = getNemIdCredentials(credentials);

        setUserName(nemIdCredentials.getUserId());
        setPassword(nemIdCredentials.getPassword());
        clickLogin();

        log.info("{} User credentials have been entered", NEM_ID_PREFIX);
        statusUpdater.updateStatusPayload(credentials, UserMessage.VERIFYING_CREDS);
    }

    private NemIdCredentials getNemIdCredentials(Credentials credentials) {
        NemIdCredentials nemIdCredentials = credentialsProvider.getNemIdCredentials(credentials);

        List<Field> fieldsToAskUserFor = nemIdCredentials.getFieldsToAskUserFor(catalog);
        if (!fieldsToAskUserFor.isEmpty()) {
            Map<String, String> supplementalInfoResponse =
                    supplementalInformationController.askSupplementalInformationSync(
                            fieldsToAskUserFor.toArray(new Field[0]));
            nemIdCredentials.setMissingCredentials(supplementalInfoResponse);
        }

        nemIdCredentials.assertNoMissingCredentials();
        return nemIdCredentials;
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
