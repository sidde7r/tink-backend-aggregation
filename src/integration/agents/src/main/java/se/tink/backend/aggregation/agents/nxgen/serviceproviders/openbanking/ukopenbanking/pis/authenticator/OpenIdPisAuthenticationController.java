package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.authenticator;

import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.OpenIdApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.OpenIdAuthenticationController;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.OpenIdAuthenticator;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.RandomValueGenerator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppStatus;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class OpenIdPisAuthenticationController extends OpenIdAuthenticationController {

    public OpenIdPisAuthenticationController(
            SupplementalInformationHelper supplementalInformationHelper,
            OpenIdApiClient apiClient,
            OpenIdAuthenticator authenticator,
            Credentials credentials,
            StrongAuthenticationState strongAuthenticationState,
            String callbackUri,
            RandomValueGenerator randomValueGenerator,
            OAuth2Token clientOAuth2Token) {
        // Do not use the real PersistentStorage because we don't want to overwrite the AIS auth
        // token.
        super(
                new PersistentStorage(),
                supplementalInformationHelper,
                apiClient,
                authenticator,
                credentials,
                strongAuthenticationState,
                callbackUri,
                null,
                randomValueGenerator);
        this.clientOAuth2Token = clientOAuth2Token;
    }

    @Override
    public ThirdPartyAppResponse<String> init() {
        return ThirdPartyAppResponseImpl.create(ThirdPartyAppStatus.WAITING);
    }
}
