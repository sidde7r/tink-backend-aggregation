package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.citadele;

import java.time.LocalDate;
import java.util.UUID;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.citadele.CitadeleBaseConstants.PathParameters;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.citadele.CitadeleBaseConstants.QueryKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.citadele.CitadeleBaseConstants.QueryValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.citadele.CitadeleBaseConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.citadele.authenticator.entities.GlobalConsentAccessEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.citadele.authenticator.rpc.ConsentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.citadele.authenticator.rpc.ConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.citadele.configuration.CitadeleBaseConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.citadele.fetcher.transactionalaccount.entities.account.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.citadele.fetcher.transactionalaccount.rpc.FetchBalancesResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.citadele.fetcher.transactionalaccount.rpc.ListAccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.citadele.fetcher.transactionalaccount.rpc.TransactionsBaseResponseEntity;
import se.tink.backend.aggregation.api.Psd2Headers;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.RandomValueGenerator;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class CitadeleBaseApiClient {

    private final TinkHttpClient client;
    private final PersistentStorage persistentStorage;
    private final RandomValueGenerator randomValueGenerator;
    private final String redirectUrl;
    private final CitadeleUserIpInformation citadeleUserIpInformation;
    private final LocalDate date;

    public CitadeleBaseApiClient(
            TinkHttpClient client,
            PersistentStorage persistentStorage,
            AgentConfiguration<CitadeleBaseConfiguration> baseConfiguration,
            RandomValueGenerator randomValueGenerator,
            CitadeleUserIpInformation citadeleUserIpInformation,
            LocalDate date) {
        this.client = client;
        this.persistentStorage = persistentStorage;
        this.redirectUrl = baseConfiguration.getRedirectUrl();
        this.randomValueGenerator = randomValueGenerator;
        this.citadeleUserIpInformation = citadeleUserIpInformation;
        this.date = date;
    }

    public ConsentResponse createConsent(String state) {
        ConsentRequest consentRequest = new ConsentRequest(new GlobalConsentAccessEntity(), date);
        return createConsent(consentRequest, state);
    }

    protected ConsentResponse createConsent(ConsentRequest consentRequest, String state) {
        return createConsentRequest(new URL(Urls.CONSENT), state)
                .post(ConsentResponse.class, consentRequest);
    }

    protected RequestBuilder createConsentRequest(URL url, String state) {

        return client.request(url)
                .header(Psd2Headers.Keys.TPP_REDIRECT_URI, createReturnUrl(state, true))
                .header(Psd2Headers.Keys.TPP_NOK_REDIRECT_URI, createReturnUrl(state, false))
                .header(Psd2Headers.Keys.X_REQUEST_ID, randomValueGenerator.getUUID())
                .header(Psd2Headers.Keys.PSU_IP_ADDRESS, citadeleUserIpInformation.getUserIp())
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON);
    }

    public ListAccountsResponse fetchAccounts() {
        return createRequestInSession(new URL(Urls.ACCOUNTS)).get(ListAccountsResponse.class);
    }

    public FetchBalancesResponse fetchBalances(AccountEntity accountEntity) {
        return createRequestInSession(
                        new URL(Urls.BALANCES)
                                .parameter(
                                        PathParameters.RESOURCE_ID, accountEntity.getResourceId()))
                .get(FetchBalancesResponse.class);
    }

    public TransactionsBaseResponseEntity getTransactions(
            String resourceId, LocalDate dateFrom, LocalDate dateTo) {
        return createRequestInSession(
                        new URL(Urls.TRANSACTIONS)
                                .parameter(PathParameters.RESOURCE_ID, resourceId))
                .queryParam(QueryKeys.DATE_FROM, dateFrom.toString())
                .queryParam(QueryKeys.DATE_TO, dateTo.toString())
                .queryParam(QueryKeys.BOOKING_STATUS, QueryValues.BOOKING_STATUS)
                .get(TransactionsBaseResponseEntity.class);
    }

    protected RequestBuilder createRequestInSession(URL url) {
        String consentId = persistentStorage.get(Psd2Headers.Keys.CONSENT_ID);
        String uuid = UUID.randomUUID().toString();
        RequestBuilder requestBuilder = client.request(url);
        requestBuilder
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON)
                .header(Psd2Headers.Keys.CONSENT_ID, consentId)
                .header(Psd2Headers.Keys.X_REQUEST_ID, uuid);
    if (citadeleUserIpInformation.isUserPresent()) {
      requestBuilder.header(Psd2Headers.Keys.PSU_IP_ADDRESS, citadeleUserIpInformation.getUserIp());
        }
        return requestBuilder;
    }

    private URL createReturnUrl(String state, boolean code) {
        return new URL(redirectUrl)
            .queryParam(Psd2Headers.Keys.STATE, state)
            .queryParam(QueryKeys.OK, String.valueOf(code)); }
}
