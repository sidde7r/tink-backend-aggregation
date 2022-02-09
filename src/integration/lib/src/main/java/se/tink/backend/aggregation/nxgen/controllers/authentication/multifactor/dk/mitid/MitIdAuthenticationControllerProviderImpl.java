package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.dk.mitid;

import se.tink.backend.aggregation.agents.contexts.StatusUpdater;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.dk.mitid.flow.MitIdScreenFlowController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.dk.mitid.flow.MitIdScreenFlowControllerModule;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.backend.aggregation.nxgen.storage.AgentTemporaryStorage;
import se.tink.integration.webdriver.service.WebDriverService;
import se.tink.integration.webdriver.service.WebDriverServiceModule;
import se.tink.libraries.credentials.service.UserAvailability;
import se.tink.libraries.i18n_aggregation.Catalog;

public class MitIdAuthenticationControllerProviderImpl
        implements MitIdAuthenticationControllerProvider {

    @Override
    public MitIdAuthenticationController createAuthController(
            Catalog catalog,
            StatusUpdater statusUpdater,
            SupplementalInformationController supplementalInformationController,
            UserAvailability userAvailability,
            AgentTemporaryStorage agentTemporaryStorage,
            MitIdAuthenticator mitIdAuthenticator) {

        WebDriverService driverService =
                WebDriverServiceModule.createWebDriverService(agentTemporaryStorage);

        MitIdScreenFlowController mitIdScreenFlowController =
                MitIdScreenFlowControllerModule.createMitIdScreenFlowController(
                        catalog,
                        statusUpdater,
                        supplementalInformationController,
                        driverService,
                        mitIdAuthenticator);

        return new MitIdAuthenticationController(
                userAvailability,
                driverService,
                agentTemporaryStorage,
                mitIdAuthenticator,
                mitIdScreenFlowController);
    }
}
