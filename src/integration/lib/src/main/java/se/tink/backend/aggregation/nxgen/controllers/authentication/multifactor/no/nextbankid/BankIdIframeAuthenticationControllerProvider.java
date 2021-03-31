package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid;

import se.tink.backend.aggregation.agents.contexts.StatusUpdater;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.libraries.i18n.Catalog;

public interface BankIdIframeAuthenticationControllerProvider {

    /**
     * This is the only correct way to initialize {@link BankIdIframeAuthenticationController} with
     * all it's dependencies.
     */
    BankIdIframeAuthenticationController createAuthController(
            Catalog catalog,
            StatusUpdater statusUpdater,
            SupplementalInformationController supplementalInformationController,
            BankIdIframeInitializer iframeInitializer,
            BankIdIframeAuthenticator iframeAuthenticator);
}
