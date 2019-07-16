package se.tink.backend.aggregation.agents.nxgen.de.openbanking.commerzbank.authenticator;

import se.tink.backend.aggregation.agents.nxgen.de.openbanking.commerzbank.CommerzbankApiClient;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.commerzbank.authenticator.rpc.ConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.Xs2aDevelopersApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.Xs2aDevelopersConstants.FormValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.Xs2aDevelopersConstants.QueryValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.Xs2aDevelopersConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.authenticator.Xs2aDevelopersAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.authenticator.entities.AccessEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.authenticator.rpc.PostConsentBody;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.configuration.Xs2aDevelopersConfiguration;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class CommerzbankAuthenticator extends Xs2aDevelopersAuthenticator {

    private CommerzbankApiClient commerzbankApiClient;

    public CommerzbankAuthenticator(
            Xs2aDevelopersApiClient apiClient,
            PersistentStorage persistentStorage,
            Xs2aDevelopersConfiguration configuration,
            String iban) {
        super(apiClient, persistentStorage, configuration, iban);
        this.commerzbankApiClient = (CommerzbankApiClient) apiClient;
    }

    @Override
    public URL buildAuthorizeUrl(String state) {

        AccessEntity accessEntity = new AccessEntity(FormValues.ALL_ACCOUNTS);
        PostConsentBody postConsentBody =
                new PostConsentBody(
                        accessEntity,
                        FormValues.FALSE,
                        FormValues.FREQUENCY_PER_DAY,
                        FormValues.TRUE,
                        FormValues.VALID_UNTIL);

        ConsentResponse consentResponse = commerzbankApiClient.getConsent(postConsentBody);
        persistentStorage.put(StorageKeys.CONSENT_ID, consentResponse.getConsentId());

        return apiClient.buildAuthorizeUrl(
                state,
                QueryValues.SCOPE + super.persistentStorage.get(StorageKeys.CONSENT_ID),
                consentResponse.getLinksEntity().getScaOAuthEntity().getHref());
    }
}
