package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid;

import se.tink.backend.aggregation.agents.contexts.SystemUpdater;
import se.tink.backend.aggregation.configuration.CallbackJwtSignatureKeyPair;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
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
            CallbackJwtSignatureKeyPair callbackJWTSignatureKeyPair) {

        OpenIdAuthenticationController openIdAuthenticationController =
                new OpenIdAuthenticationController(
                        persistentStorage,
                        supplementalInformationHelper,
                        apiClient,
                        authenticator,
                        callbackJWTSignatureKeyPair,
                        request.getCallbackRedirectUriId());

        return new AutoAuthenticationController(
                request,
                systemUpdater,
                new ThirdPartyAppAuthenticationController<>(
                        openIdAuthenticationController, supplementalInformationHelper),
                openIdAuthenticationController);
    }
}
