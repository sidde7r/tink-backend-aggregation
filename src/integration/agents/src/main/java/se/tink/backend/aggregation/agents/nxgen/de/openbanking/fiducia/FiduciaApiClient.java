package se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia;

import javax.ws.rs.core.MediaType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.FiduciaConstants.ErrorMessageKeys;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.FiduciaConstants.FormValues;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.FiduciaConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.FiduciaConstants.QueryParamsKeys;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.FiduciaConstants.QueryParamsValues;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.FiduciaConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.fetcher.transactionalaccount.rpc.GetAccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.fetcher.transactionalaccount.rpc.GetBalancesResponse;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.fetcher.transactionalaccount.rpc.GetTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.utils.ErrorChecker;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.AccessEntity;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.AuthorizationRequest;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.AuthorizationResponse;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.AuthorizationStatusResponse;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.ConsentDetailsResponse;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.ConsentRequest;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.ConsentResponse;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.FinalizeAuthorizationRequest;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.PsuDataEntity;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.SelectAuthorizationMethodRequest;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.RandomValueGenerator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

@RequiredArgsConstructor
@Slf4j
public class FiduciaApiClient {

    private static final String CONSENTS_ENDPOINT = "/v1/consents";
    private static final String CONSENT_ENDPOINT = "/v1/consents/{consentId}";
    private static final String ACCOUNTS_ENDPOINT = "/v1/accounts";
    private static final String BALANCES_ENDPOINT = "/v1/accounts/{accountId}/balances";
    private static final String TRANSACTIONS_ENDPOINT = "/v1/accounts/{accountId}/transactions";

    private static final String ACCOUNT_ID = "accountId";
    private static final String CONSENT_ID = "consentId";

    private final TinkHttpClient client;
    private final PersistentStorage persistentStorage;
    private final String userIp;
    private final String serverUrl;
    private final RandomValueGenerator randomValueGenerator;

    public URL createUrl(String path) {
        return new URL(serverUrl + "/bg13" + path);
    }

    public RequestBuilder createRequestInSession(URL url, String consentId) {
        return createRequest(url).header(FiduciaConstants.HeaderKeys.CONSENT_ID, consentId);
    }

    public RequestBuilder createRequest(URL url) {
        return client.request(url)
                .header(FiduciaConstants.HeaderKeys.ACCEPT, MediaType.APPLICATION_JSON)
                .header(FiduciaConstants.HeaderKeys.X_REQUEST_ID, randomValueGenerator.getUUID())
                .header(FiduciaConstants.HeaderKeys.PSU_IP_ADDRESS, userIp);
    }

    public ConsentResponse createConsent(String username) {
        ConsentRequest createConsentRequest =
                new ConsentRequest(
                        new AccessEntity(AccessEntity.ALL_ACCOUNTS),
                        true,
                        FormValues.VALID_UNTIL,
                        FormValues.FREQUENCY_PER_DAY,
                        false);

        try {
            return createRequest(createUrl(CONSENTS_ENDPOINT))
                    .type(MediaType.APPLICATION_JSON_TYPE)
                    .header(HeaderKeys.PSU_ID, username)
                    .post(ConsentResponse.class, createConsentRequest);
        } catch (HttpResponseException e) {
            throw ErrorChecker.errorChecker(e);
        }
    }

    public ConsentDetailsResponse getConsentDetails(String consentId) {
        return createRequest(createUrl(CONSENT_ENDPOINT).parameter(CONSENT_ID, consentId))
                .get(ConsentDetailsResponse.class);
    }

    public AuthorizationResponse authorizeWithPassword(String url, String password) {
        AuthorizationRequest authorizationRequest =
                new AuthorizationRequest(new PsuDataEntity(password));

        try {
            return createRequest(createUrl(url))
                    .type(MediaType.APPLICATION_JSON_TYPE)
                    .post(AuthorizationResponse.class, authorizationRequest);
        } catch (HttpResponseException e) {
            throw ErrorChecker.errorChecker(e);
        }
    }

    public AuthorizationResponse selectAuthMethod(String url, String scaMethodId) {
        SelectAuthorizationMethodRequest request =
                new SelectAuthorizationMethodRequest(scaMethodId);

        try {
            return createRequest(createUrl(url))
                    .type(MediaType.APPLICATION_JSON_TYPE)
                    .put(AuthorizationResponse.class, request);
        } catch (HttpResponseException e) {
            throw ErrorChecker.errorChecker(e);
        }
    }

    public AuthorizationStatusResponse authorizeWithOtp(String url, String otp) {
        FinalizeAuthorizationRequest finalizeAuthorizationRequest =
                new FinalizeAuthorizationRequest(otp);
        try {
            return createRequest(createUrl(url))
                    .type(MediaType.APPLICATION_JSON_TYPE)
                    .put(AuthorizationStatusResponse.class, finalizeAuthorizationRequest);
        } catch (HttpResponseException e) {
            if (e.getResponse()
                    .getBody(String.class)
                    .contains(ErrorMessageKeys.PSU_CREDENTIALS_INVALID)) {
                throw LoginError.INCORRECT_CHALLENGE_RESPONSE.exception();
            }
            throw e;
        }
    }

    public GetAccountsResponse getAccounts() {
        return createRequestInSession(
                        createUrl(ACCOUNTS_ENDPOINT), persistentStorage.get(StorageKeys.CONSENT_ID))
                .get(GetAccountsResponse.class);
    }

    public GetBalancesResponse getBalances(String accountId) {
        return createRequestInSession(
                        createUrl(BALANCES_ENDPOINT).parameter(ACCOUNT_ID, accountId),
                        persistentStorage.get(StorageKeys.CONSENT_ID))
                .get(GetBalancesResponse.class);
    }

    public TransactionKeyPaginatorResponse<String> getTransactions(TransactionalAccount account) {
        URL url =
                createUrl(TRANSACTIONS_ENDPOINT).parameter(ACCOUNT_ID, account.getApiIdentifier());
        String consentId = persistentStorage.get(StorageKeys.CONSENT_ID);
        return createRequestInSession(url, consentId)
                .queryParam(QueryParamsKeys.BOOKING_STATUS, QueryParamsValues.BOOKING_STATUS)
                .queryParam(QueryParamsKeys.DATE_FROM, QueryParamsValues.DATE_FROM)
                .get(GetTransactionsResponse.class);
    }

    public TransactionKeyPaginatorResponse<String> getTransactions(String continuationPath) {
        String consentId = persistentStorage.get(StorageKeys.CONSENT_ID);
        return createRequestInSession(createUrl(continuationPath), consentId)
                .get(GetTransactionsResponse.class);
    }
}
