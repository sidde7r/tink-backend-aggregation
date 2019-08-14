package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth1;

import se.tink.backend.aggregation.agents.contexts.SystemUpdater;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class OAuth1AuthenticationFlow {
    public static Authenticator create(
            CredentialsRequest request,
            SystemUpdater systemUpdater,
            PersistentStorage persistentStorage,
            SupplementalInformationHelper supplementalInformationHelper,
            OAuth1Authenticator authenticator,
            StrongAuthenticationState strongAuthenticationState) {

        OAuth1AuthenticationController oAuth1AuthenticationController =
                new OAuth1AuthenticationController(
                        persistentStorage,
                        supplementalInformationHelper,
                        authenticator,
                        strongAuthenticationState);

        return new AutoAuthenticationController(
                request,
                systemUpdater,
                new ThirdPartyAppAuthenticationController<>(
                        oAuth1AuthenticationController, supplementalInformationHelper),
                oAuth1AuthenticationController);
    }
}
