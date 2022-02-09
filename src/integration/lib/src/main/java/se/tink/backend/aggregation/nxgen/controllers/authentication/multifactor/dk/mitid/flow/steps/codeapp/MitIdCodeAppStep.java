package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.dk.mitid.flow.steps.codeapp;

import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.dk.mitid.MitIdConstants.MIT_ID_LOG_TAG;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.dk.mitid.MitIdConstants.Timeouts.CODE_APP_POLLING_RESULT_TIMEOUT;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.dk.mitid.MitIdConstants.Timeouts.CODE_APP_SCREEN_SEARCH_TIMEOUT;

import com.google.inject.Inject;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.SupplementalInfoException;
import se.tink.backend.aggregation.agents.exceptions.mitid.MitIdError;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.dk.mitid.flow.fields.MitIdCodeAppField;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.dk.mitid.flow.screens.MitIdScreen;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.dk.mitid.flow.screens.MitIdScreenQuery;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.dk.mitid.flow.screens.MitIdScreensManager;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.libraries.i18n_aggregation.Catalog;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class MitIdCodeAppStep {

    private final SupplementalInformationController supplementalInformationController;
    private final Catalog catalog;

    private final MitIdScreensManager screensManager;
    private final MitIdCodeAppPollingProxyFilter pollingProxyFilter;

    public void authenticateWithCodeApp() {
        assertIsOnCodeAppScreenWithoutErrors();
        displayPromptSync();
        waitForPollingResponse();
    }

    private void assertIsOnCodeAppScreenWithoutErrors() {
        screensManager.searchForFirstScreen(
                MitIdScreenQuery.builder()
                        .searchForExpectedScreens(MitIdScreen.CODE_APP_SCREEN)
                        .searchForSeconds(CODE_APP_SCREEN_SEARCH_TIMEOUT)
                        .build());
    }

    private void displayPromptSync() {
        List<Field> fields = MitIdCodeAppField.build(catalog);
        try {
            supplementalInformationController.askSupplementalInformationSync(
                    fields.toArray(new Field[0]));
        } catch (SupplementalInfoException e) {
            // ignore empty response!
            // we're actually not interested in response at all, we just show a text!
        }
    }

    private void waitForPollingResponse() {
        log.info("{} Awaiting code app polling response", MIT_ID_LOG_TAG);
        MitIdCodeAppPollingResult pollingResult =
                pollingProxyFilter
                        .waitForResult(CODE_APP_POLLING_RESULT_TIMEOUT)
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                "No MitID code app polling response"));

        switch (pollingResult) {
            case OK:
                return;
            case EXPIRED:
                throw MitIdError.CODE_APP_TIMEOUT.exception();
            case REJECTED:
                throw MitIdError.CODE_APP_REJECTED.exception();
            case TECHNICAL_ERROR:
                throw MitIdError.CODE_APP_TECHNICAL_ERROR.exception();
            case UNKNOWN:
                throw new IllegalStateException("Unknown MitID code app polling result.");
            default:
                throw new IllegalStateException(
                        "Unexpected MitID code polling result: " + pollingResult);
        }
    }
}
