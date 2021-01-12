package se.tink.backend.aggregation.agents.nxgen.de.openbanking.commerzbank.authenticator;

import java.time.format.DateTimeFormatter;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.commerzbank.CommerzbankApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.Xs2aDevelopersApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.Xs2aDevelopersConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.authenticator.Xs2aDevelopersAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.authenticator.entities.AccessEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.authenticator.rpc.ConsentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.authenticator.rpc.ConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.configuration.Xs2aDevelopersProviderConfiguration;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class CommerzbankAuthenticator extends Xs2aDevelopersAuthenticator {
    private final CommerzbankApiClient commerzbankApiClient;

    public CommerzbankAuthenticator(
            Xs2aDevelopersApiClient apiClient,
            PersistentStorage persistentStorage,
            Xs2aDevelopersProviderConfiguration configuration,
            LocalDateTimeSource localDateTimeSource,
            Credentials credentials,
            CommerzbankApiClient commerzbankApiClient) {
        super(apiClient, persistentStorage, configuration, localDateTimeSource, credentials);
        this.commerzbankApiClient = commerzbankApiClient;
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
        String authorizationEndpoint =
                commerzbankApiClient.getAuthorizationEndpoint(scaOAuthSourceUrl);
        persistentStorage.put(
                Xs2aDevelopersConstants.StorageKeys.CONSENT_ID, consentResponse.getConsentId());
        return apiClient.buildAuthorizeUrl(
                state,
                Xs2aDevelopersConstants.QueryValues.SCOPE + consentResponse.getConsentId(),
                authorizationEndpoint);
    }
}
