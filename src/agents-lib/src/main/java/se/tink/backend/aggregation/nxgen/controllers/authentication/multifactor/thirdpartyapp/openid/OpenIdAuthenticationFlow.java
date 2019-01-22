package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.rpc.CredentialsRequest;

public class OpenIdAuthenticationFlow {
    public static Authenticator create(CredentialsRequest request, AgentContext context,
            PersistentStorage persistentStorage, SupplementalInformationHelper supplementalInformationHelper,
            OpenIdAuthenticator authenticator, OpenIdApiClient apiClient) {

        OpenIdAuthenticationController openIdAuthenticationController = new OpenIdAuthenticationController(
                persistentStorage,
                supplementalInformationHelper,
                apiClient,
                authenticator
        );

        return new AutoAuthenticationController(
                request,
                context,
                new ThirdPartyAppAuthenticationController<>(
                        openIdAuthenticationController,
                        supplementalInformationHelper
                ),
                openIdAuthenticationController
        );
    }
}
