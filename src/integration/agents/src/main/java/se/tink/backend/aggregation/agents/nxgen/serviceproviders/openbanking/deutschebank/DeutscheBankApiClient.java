package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank;

import com.google.common.base.Strings;
import java.util.Optional;
import javax.ws.rs.core.MediaType;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.DeutscheBankConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.DeutscheBankConstants.IdKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.DeutscheBankConstants.Parameters;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.DeutscheBankConstants.QueryKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.DeutscheBankConstants.QueryValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.DeutscheBankConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.DeutscheBankConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.authenticator.entities.GlobalConsentAccessEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.authenticator.rpc.AuthorisationDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.authenticator.rpc.ConsentDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.authenticator.rpc.ConsentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.authenticator.rpc.ConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.authenticator.rpc.ConsentStatusResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.configuration.DeutscheMarketConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.fetcher.transactionalaccount.entity.account.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.fetcher.transactionalaccount.rpc.account.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.fetcher.transactionalaccount.rpc.account.FetchBalancesResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.fetcher.transactionalaccount.rpc.transactions.TransactionsKeyPaginatorBaseResponse;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.RandomValueGenerator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

@RequiredArgsConstructor
public class DeutscheBankApiClient {

    protected final TinkHttpClient client;
    protected final PersistentStorage persistentStorage;
    protected final DeutscheHeaderValues headerValues;
    protected final DeutscheMarketConfiguration marketConfiguration;
    protected final RandomValueGenerator randomValueGenerator;
    protected final LocalDateTimeSource localDateTimeSource;

    protected RequestBuilder createRequest(URL url) {
        return client.request(url)
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON)
                .header(HeaderKeys.PSU_IP_ADDRESS, headerValues.getUserIp())
                .header(HeaderKeys.X_REQUEST_ID, randomValueGenerator.getUUID().toString());
    }

    protected RequestBuilder createRequestWithServiceMapped(URL url) {
        if (url.get().contains("{" + Parameters.SERVICE_KEY + "}")) {
            url = url.parameter(Parameters.SERVICE_KEY, Parameters.AIS);
        }
        return createRequest(url);
    }

    protected RequestBuilder createRequestInSession(URL url) {
        String consentId = persistentStorage.get(StorageKeys.CONSENT_ID);

        return createRequestWithServiceMapped(url).header(HeaderKeys.CONSENT_ID, consentId);
    }

    public ConsentResponse getConsent(String state, String psuId) {
        ConsentRequest consentRequest =
                new ConsentRequest(new GlobalConsentAccessEntity(), localDateTimeSource);
        return getConsent(consentRequest, state, psuId);
    }

    protected ConsentResponse getConsent(
            ConsentRequest consentRequest, String state, String psuId) {
        URL redirectWithState =
                new URL(headerValues.getRedirectUrl()).queryParam(QueryKeys.STATE, state);
        return createRequestWithServiceMapped(
                        new URL(marketConfiguration.getBaseUrl().concat(Urls.CONSENT)))
                .header(HeaderKeys.PSU_ID_TYPE, marketConfiguration.getPsuIdType())
                .header(HeaderKeys.PSU_ID, psuId)
                .header(HeaderKeys.TPP_REDIRECT_URI, redirectWithState)
                .header(HeaderKeys.TPP_NOK_REDIRECT_URI, redirectWithState)
                .post(ConsentResponse.class, consentRequest);
    }

    public ConsentDetailsResponse getConsentDetails() {
        String consentId =
                Optional.ofNullable(persistentStorage.get(StorageKeys.CONSENT_ID))
                        .orElseThrow(SessionError.SESSION_EXPIRED::exception);
        return createRequestWithServiceMapped(
                        new URL(marketConfiguration.getBaseUrl() + Urls.CONSENT_DETAILS)
                                .parameter(IdKeys.CONSENT_ID, consentId))
                .get(ConsentDetailsResponse.class);
    }

    public ConsentStatusResponse getConsentStatus() {
        String consentId =
                Optional.ofNullable(persistentStorage.get(StorageKeys.CONSENT_ID))
                        .orElseThrow(SessionError.SESSION_EXPIRED::exception);
        return createRequestWithServiceMapped(
                        new URL(marketConfiguration.getBaseUrl() + Urls.CONSENTS_STATUS)
                                .parameter(IdKeys.CONSENT_ID, consentId))
                .get(ConsentStatusResponse.class);
    }

    public AuthorisationDetailsResponse getAuthorisationDetails(String url) {
        return createRequest(new URL(url)).get(AuthorisationDetailsResponse.class);
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

        RequestBuilder requestBuilder;

        if (Strings.isNullOrEmpty(key)) {
            requestBuilder = createTransactionRequest(account);
        } else {
            requestBuilder = createRequestInSession(new URL(key));
        }

        return addTransactionQueryParams(requestBuilder)
                .get(TransactionsKeyPaginatorBaseResponse.class);
    }

    protected RequestBuilder addTransactionQueryParams(RequestBuilder requestBuilder) {
        return requestBuilder
                .queryParam(QueryKeys.BOOKING_STATUS, QueryValues.BOTH)
                .queryParam(QueryKeys.DELTA_LIST, QueryValues.DELTA_LIST);
    }

    private RequestBuilder createTransactionRequest(TransactionalAccount account) {
        String key =
                marketConfiguration
                        .getBaseUrl()
                        .concat(String.format(Urls.TRANSACTIONS, account.getApiIdentifier()));
        return createRequestInSession(new URL(key));
    }
}
