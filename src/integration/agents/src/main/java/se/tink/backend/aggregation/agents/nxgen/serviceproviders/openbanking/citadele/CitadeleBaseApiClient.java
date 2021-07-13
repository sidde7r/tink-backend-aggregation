package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.citadele;

import java.time.LocalDate;
import java.util.UUID;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.citadele.CitadeleBaseConstans.Errors;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.citadele.CitadeleBaseConstans.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.citadele.CitadeleBaseConstans.PathParameter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.citadele.CitadeleBaseConstans.QueryKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.citadele.CitadeleBaseConstans.QueryValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.citadele.CitadeleBaseConstans.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.citadele.CitadeleBaseConstans.Urls;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.citadele.authenticator.entity.GlobalConsentAccessEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.citadele.authenticator.rpc.ConsentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.citadele.authenticator.rpc.ConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.citadele.configuration.CitadeleBaseConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.citadele.fetcher.transactionalaccount.entity.account.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.citadele.fetcher.transactionalaccount.entity.transaction.TransactionsBaseResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.citadele.fetcher.transactionalaccount.rpc.FetchBalancesResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.citadele.fetcher.transactionalaccount.rpc.ListAccountsResponse;
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

    public CitadeleBaseApiClient(
            TinkHttpClient client,
            PersistentStorage persistentStorage,
            AgentConfiguration<CitadeleBaseConfiguration> baseConfiguration,
            RandomValueGenerator randomValueGenerator,
            CitadeleUserIpInformation citadeleUserIpInformation) {
        this.client = client;
        this.persistentStorage = persistentStorage;
        this.redirectUrl = baseConfiguration.getRedirectUrl();
        this.randomValueGenerator = randomValueGenerator;
        this.citadeleUserIpInformation = citadeleUserIpInformation;
    }

    public ConsentResponse getConsent(String state) {
        ConsentRequest consentRequest = new ConsentRequest(new GlobalConsentAccessEntity());
        return getConsent(consentRequest, state);
    }

    protected ConsentResponse getConsent(ConsentRequest consentRequest, String state) {
        return createConsentRequest(new URL(Urls.CONSENT), state)
                .post(ConsentResponse.class, consentRequest);
    }

    protected RequestBuilder createConsentRequest(URL url, String state) {

        String code = UUID.randomUUID().toString();
        persistentStorage.put(StorageKeys.CODE, code);

        String baseUrl =
                redirectUrl
                        .concat("?")
                        .concat(QueryKeys.STATE)
                        .concat("=")
                        .concat(state)
                        .concat("&")
                        .concat(QueryKeys.CODE)
                        .concat("=");

        URL okRedirectUrl = new URL(baseUrl + code);
        URL nokRedirectUrl = new URL(baseUrl + Errors.ERROR);

        return client.request(url)
                .header(HeaderKeys.TPP_REDIRECT_URI, okRedirectUrl)
                .header(HeaderKeys.TPP_NOK_REDIRECT_URI, nokRedirectUrl)
                .header(HeaderKeys.X_REQUEST_ID, randomValueGenerator.getUUID())
                .header(HeaderKeys.PSU_IP_ADDRESS, citadeleUserIpInformation.getUserIp())
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
                                        PathParameter.RESOURCE_ID, accountEntity.getResourceId()))
                .get(FetchBalancesResponse.class);
    }

    public TransactionsBaseResponse getTransactions(
            String resourceId, LocalDate dateFrom, LocalDate dateTo) {
        return createRequestInSession(
                        new URL(Urls.TRANSACTIONS).parameter(PathParameter.RESOURCE_ID, resourceId))
                .queryParam(QueryKeys.DATE_FROM, dateFrom.toString())
                .queryParam(QueryKeys.DATE_TO, dateTo.toString())
                .queryParam(QueryKeys.BOOKING_STATUS, QueryValues.BOOKING_STATUS)
                .get(TransactionsBaseResponse.class);
    }

    protected RequestBuilder createRequestInSession(URL url) {
        String consentId = persistentStorage.get(StorageKeys.CONSENT_ID);
        String uuid = UUID.randomUUID().toString();
        RequestBuilder requestBuilder = client.request(url);
        requestBuilder
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON)
                .header(HeaderKeys.PSU_IP_ADDRESS, citadeleUserIpInformation.getUserIp())
                .header(HeaderKeys.CONSENT_ID, consentId)
                .header(HeaderKeys.X_REQUEST_ID, uuid);
        return requestBuilder;
    }
}
