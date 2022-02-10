package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.dk.mitid;

import se.tink.backend.aggregation.agents.contexts.StatusUpdater;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.backend.aggregation.nxgen.storage.AgentTemporaryStorage;
import se.tink.libraries.credentials.service.UserAvailability;
import se.tink.libraries.i18n_aggregation.Catalog;

public interface MitIdAuthenticationControllerProvider {

    /**
     * This is the only correct way to initialize {@link MitIdAuthenticationController} with all its
     * dependencies.
     */
    MitIdAuthenticationController createAuthController(
            Catalog catalog,
            StatusUpdater statusUpdater,
            SupplementalInformationController supplementalInformationController,
            UserAvailability userAvailability,
            AgentTemporaryStorage agentTemporaryStorage,
            MitIdAuthenticator mitIdAuthenticator);
}
