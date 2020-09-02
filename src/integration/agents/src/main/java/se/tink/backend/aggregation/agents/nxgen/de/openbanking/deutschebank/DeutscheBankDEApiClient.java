package se.tink.backend.aggregation.agents.nxgen.de.openbanking.deutschebank;

import java.util.UUID;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.deutschebank.authenticator.rpc.ConsentBaseRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.DeutscheBankApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.DeutscheBankConstants.Configuration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.DeutscheBankConstants.FormValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.DeutscheBankConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.DeutscheBankConstants.QueryKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.DeutscheBankConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.authenticator.rpc.ConsentBaseResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.configuration.DeutscheMarketConfiguration;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class DeutscheBankDEApiClient extends DeutscheBankApiClient {

    DeutscheBankDEApiClient(
            TinkHttpClient client,
            PersistentStorage persistentStorage,
            String redirectUrl,
            DeutscheMarketConfiguration marketConfiguration) {
        super(client, persistentStorage, redirectUrl, marketConfiguration);
    }

    @Override
    public ConsentBaseResponse getConsent(String state, String iban, String psuId) {
        String currency = FormValues.CURRENCY_TYPE;
        ConsentBaseRequest consentBaseRequest = new ConsentBaseRequest(iban, currency);
        return client.request(new URL(marketConfiguration.getBaseUrl().concat(Urls.CONSENT)))
                .header(HeaderKeys.X_REQUEST_ID, UUID.randomUUID().toString())
                .header(HeaderKeys.PSU_ID_TYPE, marketConfiguration.getPsuIdType())
                .header(HeaderKeys.PSU_ID, psuId)
                .header(HeaderKeys.PSU_IP_ADDRESS, Configuration.PSU_IP_ADDRESS)
                .header(
                        HeaderKeys.TPP_REDIRECT_URI,
                        new URL(redirectUrl).queryParam(QueryKeys.STATE, state))
                .header(
                        HeaderKeys.TPP_NOK_REDIRECT_URI,
                        new URL(redirectUrl).queryParam(QueryKeys.STATE, state))
                .type(MediaType.APPLICATION_JSON)
                .post(ConsentBaseResponse.class, consentBaseRequest);
    }
}
