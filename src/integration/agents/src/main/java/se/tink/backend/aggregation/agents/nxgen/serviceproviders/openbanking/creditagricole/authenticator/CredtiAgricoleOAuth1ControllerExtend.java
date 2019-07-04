package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.authenticator;

import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.CreditAgricoleApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.postauthentication.CreditAgricolePostAuthentication;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth1.OAuth1AuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth1.OAuth1Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class CredtiAgricoleOAuth1ControllerExtend extends OAuth1AuthenticationController {

    private final CreditAgricoleApiClient apiClient;
    private final CreditAgricolePostAuthentication postAuthentication;

    public CredtiAgricoleOAuth1ControllerExtend(
            PersistentStorage persistentStorage,
            SupplementalInformationHelper supplementalInformationHelper,
            OAuth1Authenticator authenticator,
            CreditAgricoleApiClient apiClient) {
        super(persistentStorage, supplementalInformationHelper, authenticator);

        this.apiClient = apiClient;
        this.postAuthentication = new CreditAgricolePostAuthentication();
    }

    @Override
    public ThirdPartyAppResponse<String> collect(String reference)
            throws AuthenticationException, AuthorizationException {
        ThirdPartyAppResponse<String> superReturn = super.collect(reference);

        postAuthentication.getAdditionalParamsPostAuthentication(apiClient);

        return superReturn;
    }
}
