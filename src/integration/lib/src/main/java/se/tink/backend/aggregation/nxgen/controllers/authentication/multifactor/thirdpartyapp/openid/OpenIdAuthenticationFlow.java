package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid;

import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.contexts.SystemUpdater;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.RandomValueGenerator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class OpenIdAuthenticationFlow {

    public static Authenticator create(
            CredentialsRequest request,
            SystemUpdater systemUpdater,
            PersistentStorage persistentStorage,
            SupplementalInformationHelper supplementalInformationHelper,
            OpenIdAuthenticator authenticator,
            OpenIdApiClient apiClient,
            Credentials credentials,
            StrongAuthenticationState strongAuthenticationState,
            URL appToAppRedirectURL,
            RandomValueGenerator randomValueGenerator) {

        OpenIdAuthenticationController openIdAuthenticationController =
                new OpenIdAuthenticationController(
                        persistentStorage,
                        supplementalInformationHelper,
                        apiClient,
                        authenticator,
                        credentials,
                        strongAuthenticationState,
                        request.getCallbackUri(),
                        appToAppRedirectURL,
                        randomValueGenerator,
                        request);

        return new AutoAuthenticationController(
                request,
                systemUpdater,
                new ThirdPartyAppAuthenticationController<>(
                        openIdAuthenticationController, supplementalInformationHelper),
                openIdAuthenticationController);
    }
}
