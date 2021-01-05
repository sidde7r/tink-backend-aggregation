package se.tink.backend.aggregation.agents.nxgen.be.openbanking.bpost.authenticator;

import com.google.common.base.Strings;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.errors.ThirdPartyAppError;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.bpost.BPostApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.Xs2aDevelopersApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.Xs2aDevelopersConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.authenticator.Xs2aDevelopersAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.authenticator.entities.AccessEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.authenticator.rpc.ConsentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.authenticator.rpc.ConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.configuration.Xs2aDevelopersProviderConfiguration;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.constants.OAuth2Constants.CallbackParams;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class BPostAuthenticator extends Xs2aDevelopersAuthenticator {

    private final BPostApiClient bPostApiClient;

    public BPostAuthenticator(
            Xs2aDevelopersApiClient apiClient,
            PersistentStorage persistentStorage,
            Xs2aDevelopersProviderConfiguration configuration,
            LocalDateTimeSource localDateTimeSource,
            Credentials credentials,
            BPostApiClient bPostApiClient) {
        super(apiClient, persistentStorage, configuration, localDateTimeSource, credentials);
        this.bPostApiClient = bPostApiClient;
    }

    @Override
    public URL buildAuthorizeUrl(String state) {
        AccessEntity accessEntity = getAccessEntity();
        ConsentRequest consentRequest =
                new ConsentRequest(
                        accessEntity,
                        Xs2aDevelopersConstants.FormValues.FALSE,
                        Xs2aDevelopersConstants.FormValues.FREQUENCY_PER_DAY,
                        Xs2aDevelopersConstants.FormValues.TRUE,
                        localDateTimeSource.now().plusDays(89).format(DateTimeFormatter.ISO_DATE));

        ConsentResponse consentResponse = apiClient.createConsent(consentRequest);
        String scaOAuthSourceUrl = consentResponse.getLinks().getScaOAuth();
        String authorizationEndpoint = bPostApiClient.getAuthorizationEndpoint(scaOAuthSourceUrl);
        persistentStorage.put(
                Xs2aDevelopersConstants.StorageKeys.CONSENT_ID, consentResponse.getConsentId());
        return apiClient.buildAuthorizeUrl(
                state,
                Xs2aDevelopersConstants.QueryValues.SCOPE + consentResponse.getConsentId(),
                authorizationEndpoint);
    }

    @Override
    public void handleSpecificCallbackDataError(Map<String, String> callbackData)
            throws AuthenticationException {
        String value = callbackData.getOrDefault(CallbackParams.ERROR, null);
        if (!Strings.isNullOrEmpty(value)
                && value.contains(
                        "The Strong Customer Authentication solution encountered an error")) {
            throw ThirdPartyAppError.AUTHENTICATION_ERROR.exception();
        }
    }
}
