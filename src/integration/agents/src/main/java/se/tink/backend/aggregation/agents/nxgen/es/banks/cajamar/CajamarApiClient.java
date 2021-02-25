package se.tink.backend.aggregation.agents.nxgen.es.banks.cajamar;

import javax.ws.rs.core.MediaType;
import org.assertj.core.util.Strings;
import se.tink.backend.aggregation.agents.nxgen.es.banks.cajamar.CajamarConstants.AuthenticationKeys;
import se.tink.backend.aggregation.agents.nxgen.es.banks.cajamar.CajamarConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.es.banks.cajamar.CajamarConstants.HeaderValues;
import se.tink.backend.aggregation.agents.nxgen.es.banks.cajamar.CajamarConstants.SessionKeys;
import se.tink.backend.aggregation.agents.nxgen.es.banks.cajamar.CajamarConstants.URLs;
import se.tink.backend.aggregation.agents.nxgen.es.banks.cajamar.authenticator.rpc.EnrollmentRequest;
import se.tink.backend.aggregation.agents.nxgen.es.banks.cajamar.authenticator.rpc.EnrollmentResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.cajamar.authenticator.rpc.LoginRequest;
import se.tink.backend.aggregation.agents.nxgen.es.banks.cajamar.authenticator.rpc.LoginResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.cajamar.entities.AccountDetailsEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.cajamar.entities.PositionEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.cajamar.fetcher.account.rpc.CajamarAccountTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.cajamar.fetcher.creditcard.rpc.CajamarCreditCardTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.cajamar.fetcher.creditcard.rpc.CreditCardResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.cajamar.session.KeepAliveRequest;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class CajamarApiClient {

    private final TinkHttpClient client;
    private final SessionStorage sessionStorage;

    public CajamarApiClient(TinkHttpClient client, SessionStorage sessionStorage) {
        this.client = client;
        this.sessionStorage = sessionStorage;
    }

    public HttpResponse isAlive() {
        return createRequestIsAlive(URLs.UPDATE_PUSH_TOKEN)
                .post(HttpResponse.class, new KeepAliveRequest(getPushToken()));
    }

    public EnrollmentResponse fetchEnrollment(EnrollmentRequest request) {
        EnrollmentResponse enrollmentResponse =
                createEnrollmentRequest().post(EnrollmentResponse.class, request);
        addAccessTokenToSessionStorage(enrollmentResponse);
        addPushTokenToSessionStorage(request.getPushToken());
        return enrollmentResponse;
    }

    public LoginResponse login(LoginRequest loginRequest) {
        return createLoginRequest().post(LoginResponse.class, loginRequest);
    }

    public void logout() {
        createAuthorizedRequest(URLs.LOGOUT).post(HttpResponse.class);
    }

    public PositionEntity fetchPositions() {
        return createAuthorizedRequest(URLs.POSITIONS).post(PositionEntity.class);
    }

    public AccountDetailsEntity fetchAccountInfo(String accountId) {
        return createAuthorizedRequest(
                        URL.of(URLs.ACCOUNT).parameter(URLs.PARAM_ID, accountId).get())
                .post(AccountDetailsEntity.class);
    }

    public CajamarAccountTransactionsResponse fetchAccountTransactions(
            TransactionalAccount account, String key) {
        return createAccountTransactionsRequest(account.getApiIdentifier(), key)
                .post(CajamarAccountTransactionsResponse.class);
    }

    public CreditCardResponse fetchCreditCardDetails(String cardId) {
        return createAuthorizedRequest(
                        URL.of(URLs.CREDIT_CARD).parameter(URLs.PARAM_ID, cardId).get())
                .post(CreditCardResponse.class);
    }

    public CajamarCreditCardTransactionsResponse fetchCreditCardTransactions(
            CreditCardAccount account, String key) {
        return createCardTransactionsRequest(account.getApiIdentifier(), key)
                .post(CajamarCreditCardTransactionsResponse.class);
    }

    private RequestBuilder createCardTransactionsRequest(String cardId, String key) {
        if (Strings.isNullOrEmpty(key)) {
            return createAuthorizedRequest(
                            URL.of(URLs.CARD_TRANSACTIONS).parameter(URLs.PARAM_ID, cardId).get())
                    .queryParam(
                            CajamarConstants.QueryParams.PAGE_NUMBER,
                            CajamarConstants.QueryValues.FIRST_PAGE);
        }
        return createAuthorizedRequest(
                        URL.of(URLs.CARD_TRANSACTIONS).parameter(URLs.PARAM_ID, cardId).get())
                .queryParam(CajamarConstants.QueryParams.PAGE_NUMBER, key);
    }

    private RequestBuilder createAccountTransactionsRequest(String accountId, String key) {
        if (Strings.isNullOrEmpty(key)) {
            return createAuthorizedRequest(
                            URL.of(URLs.ACCOUNT_TRANSACTIONS)
                                    .parameter(URLs.PARAM_ID, accountId)
                                    .get())
                    .queryParam(
                            CajamarConstants.QueryParams.PAGE_NUMBER,
                            CajamarConstants.QueryValues.FIRST_PAGE);
        }
        return createAuthorizedRequest(
                        URL.of(URLs.ACCOUNT_TRANSACTIONS).parameter(URLs.PARAM_ID, accountId).get())
                .queryParam(CajamarConstants.QueryParams.PAGE_NUMBER, key);
    }

    private RequestBuilder createLoginRequest() {
        return createRequest(URLs.LOGIN)
                .header(CajamarConstants.HeaderKeys.AUTHORIZATION, getBearerToken());
    }

    private RequestBuilder createAuthorizedRequest(String url) {
        return createRequest(url)
                .header(CajamarConstants.HeaderKeys.AUTHORIZATION, getBearerToken());
    }

    private RequestBuilder createEnrollmentRequest() {
        return createRequest(URLs.ENROLLMENT);
    }

    private RequestBuilder createRequest(String url) {
        return client.request(url)
                .header(HeaderKeys.USER_AGENT, HeaderValues.USER_AGENT_VALUE)
                .type(MediaType.APPLICATION_JSON)
                .accept("*/*");
    }

    private RequestBuilder createRequestIsAlive(String url) {
        return client.request(url)
                .header(CajamarConstants.HeaderKeys.AUTHORIZATION, getBearerToken())
                .header(HeaderKeys.USER_AGENT, HeaderValues.USER_AGENT_VALUE)
                .type(MediaType.APPLICATION_JSON)
                .accept("*/*");
    }

    private void addPushTokenToSessionStorage(String pushToken) {
        sessionStorage.put(SessionKeys.PUSH_TOKEN, pushToken);
    }

    public String getPushToken() {
        return sessionStorage.get(SessionKeys.PUSH_TOKEN);
    }

    private void addAccessTokenToSessionStorage(EnrollmentResponse enrollmentResponse) {
        sessionStorage.put(
                AuthenticationKeys.BEARER_TOKEN, "Bearer " + enrollmentResponse.getAccessToken());
    }

    public String getBearerToken() {
        return sessionStorage.get(AuthenticationKeys.BEARER_TOKEN);
    }
}
