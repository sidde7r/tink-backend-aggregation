package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2;

import se.tink.backend.aggregation.agents.contexts.SystemUpdater;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class OAuth2AuthenticationFlow {
    public static Authenticator create(CredentialsRequest request, SystemUpdater systemUpdater,
            PersistentStorage persistentStorage, SupplementalInformationHelper supplementalInformationHelper,
            OAuth2Authenticator authenticator) {

        OAuth2AuthenticationController oAuth2AuthenticationController = new OAuth2AuthenticationController(
                persistentStorage,
                supplementalInformationHelper,
                authenticator
        );

        return new AutoAuthenticationController(
                request,
                systemUpdater,
                new ThirdPartyAppAuthenticationController<>(
                        oAuth2AuthenticationController,
                        supplementalInformationHelper
                ),
                oAuth2AuthenticationController
        );
    }
}
