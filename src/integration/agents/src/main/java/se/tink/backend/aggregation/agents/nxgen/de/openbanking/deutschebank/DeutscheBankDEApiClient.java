package se.tink.backend.aggregation.agents.nxgen.de.openbanking.deutschebank;

import java.util.UUID;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.deutschebank.authenticator.rpc.ConsentBaseRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.DeutscheBankApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.DeutscheBankConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.authenticator.rpc.ConsentBaseResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.configuration.DeutscheBankConfiguration;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class DeutscheBankDEApiClient extends DeutscheBankApiClient {

    private final TinkHttpClient client;
    private final SessionStorage sessionStorage;
    private final DeutscheBankConfiguration configuration;
    private final String redirectUrl;

    public DeutscheBankDEApiClient(
            TinkHttpClient client,
            SessionStorage sessionStorage,
            AgentConfiguration<DeutscheBankConfiguration> agentConfiguration) {
        super(client, sessionStorage, agentConfiguration);
        this.client = client;
        this.sessionStorage = sessionStorage;
        this.configuration = agentConfiguration.getProviderSpecificConfiguration();
        this.redirectUrl = agentConfiguration.getRedirectUrl();
    }

    @Override
    public ConsentBaseResponse getConsent(String state, String iban, String psuId) {
        String currency = DeutscheBankConstants.FormValues.CURRENCY_TYPE;
        ConsentBaseRequest consentBaseRequest = new ConsentBaseRequest(iban, currency);
        return client.request(
                        new URL(
                                configuration
                                        .getBaseUrl()
                                        .concat(DeutscheBankConstants.Urls.CONSENT)))
                .header(DeutscheBankConstants.HeaderKeys.X_REQUEST_ID, UUID.randomUUID().toString())
                .header(DeutscheBankConstants.HeaderKeys.PSU_ID_TYPE, configuration.getPsuIdType())
                .header(DeutscheBankConstants.HeaderKeys.PSU_ID, psuId)
                .header(
                        DeutscheBankConstants.HeaderKeys.PSU_IP_ADDRESS,
                        configuration.getPsuIpAddress())
                .header(
                        DeutscheBankConstants.HeaderKeys.TPP_REDIRECT_URI,
                        new URL(redirectUrl)
                                .queryParam(DeutscheBankConstants.QueryKeys.STATE, state))
                .header(
                        DeutscheBankConstants.HeaderKeys.TPP_NOK_REDIRECT_URI,
                        new URL(redirectUrl)
                                .queryParam(DeutscheBankConstants.QueryKeys.STATE, state))
                .type(MediaType.APPLICATION_JSON)
                .post(ConsentBaseResponse.class, consentBaseRequest);
    }
}
