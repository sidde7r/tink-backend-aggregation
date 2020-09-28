package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank;

import com.google.common.base.Strings;
import java.util.UUID;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.DeutscheBankConstants.Configuration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.DeutscheBankConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.DeutscheBankConstants.QueryKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.DeutscheBankConstants.QueryValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.DeutscheBankConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.DeutscheBankConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.authenticator.entities.GlobalConsentAccessEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.authenticator.rpc.ConsentBaseRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.authenticator.rpc.ConsentBaseResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.authenticator.rpc.ConsentStatusResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.configuration.DeutscheMarketConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.fetcher.transactionalaccount.entity.account.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.fetcher.transactionalaccount.rpc.account.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.fetcher.transactionalaccount.rpc.account.FetchBalancesResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.fetcher.transactionalaccount.rpc.transactions.TransactionsKeyPaginatorBaseResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class DeutscheBankApiClient {

    protected final TinkHttpClient client;
    protected final PersistentStorage persistentStorage;
    protected final DeutscheMarketConfiguration marketConfiguration;
    protected final String redirectUrl;

    public DeutscheBankApiClient(
            TinkHttpClient client,
            PersistentStorage persistentStorage,
            String redirectUrl,
            DeutscheMarketConfiguration marketConfiguration) {
        this.client = client;
        this.persistentStorage = persistentStorage;
        this.redirectUrl = redirectUrl;
        this.marketConfiguration = marketConfiguration;
    }

    protected RequestBuilder createRequest(URL url) {
        return client.request(url)
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON);
    }

    protected RequestBuilder createRequestInSession(URL url) {
        String consentId = persistentStorage.get(StorageKeys.CONSENT_ID);
        String uuid = UUID.randomUUID().toString();

        return createRequest(url)
                .header(HeaderKeys.CONSENT_ID, consentId)
                .header(HeaderKeys.X_REQUEST_ID, uuid);
    }

    public ConsentBaseResponse getConsent(String state, String psuId) {
        ConsentBaseRequest consentBaseRequest =
                new ConsentBaseRequest(new GlobalConsentAccessEntity());
        return getConsent(consentBaseRequest, state, psuId);
    }

    protected ConsentBaseResponse getConsent(
            ConsentBaseRequest consentBaseRequest, String state, String psuId) {
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

    public ConsentStatusResponse getConsentStatus() {
        String consentId = persistentStorage.get(StorageKeys.CONSENT_ID);
        return createRequest(
                        new URL(
                                marketConfiguration
                                        .getBaseUrl()
                                        .concat(String.format(Urls.STATUS, consentId))))
                .header(HeaderKeys.X_REQUEST_ID, UUID.randomUUID().toString())
                .header(HeaderKeys.PSU_IP_ADDRESS, Configuration.PSU_IP_ADDRESS)
                .get(ConsentStatusResponse.class);
    }

    public FetchAccountsResponse fetchAccounts() {
        return createRequestInSession(
                        new URL(marketConfiguration.getBaseUrl().concat(Urls.ACCOUNTS)))
                .get(FetchAccountsResponse.class);
    }

    public FetchBalancesResponse fetchBalances(AccountEntity accountEntity) {
        return createRequestInSession(
                        new URL(
                                marketConfiguration
                                        .getBaseUrl()
                                        .concat(
                                                String.format(
                                                        Urls.BALANCES,
                                                        accountEntity.getResourceId()))))
                .get(FetchBalancesResponse.class);
    }

    public TransactionKeyPaginatorResponse<String> fetchTransactionsForAccount(
            TransactionalAccount account, String key) {

        if (Strings.isNullOrEmpty(key)) {
            key =
                    marketConfiguration
                            .getBaseUrl()
                            .concat(String.format(Urls.TRANSACTIONS, account.getApiIdentifier()));
        }

        return createRequestInSession(new URL(key))
                .queryParam(QueryKeys.BOOKING_STATUS, QueryValues.BOOKING_STATUS)
                .queryParam(QueryKeys.DELTA_LIST, QueryValues.DELTA_LIST)
                .get(TransactionsKeyPaginatorBaseResponse.class);
    }
}
