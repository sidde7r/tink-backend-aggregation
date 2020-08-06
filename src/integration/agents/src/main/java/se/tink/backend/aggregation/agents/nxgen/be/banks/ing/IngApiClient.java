package se.tink.backend.aggregation.agents.nxgen.be.banks.ing;

import java.util.Optional;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.entities.LoginResponseEntity;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.entities.MobileHelloResponseEntity;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.entities.PrepareEnrollResponseEntity;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.rpc.AppCredentialsBody;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.rpc.AppCredentialsResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.rpc.BaseBody;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.rpc.ConfirmEnrollRequestBody;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.rpc.InitEnrollRequestBody;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.rpc.LoginRequestBody;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.rpc.LoginResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.rpc.MobileHelloRequestBody;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.rpc.MobileHelloResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.rpc.PrepareEnrollResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.rpc.TrustBuilderRequestBody;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.entites.json.BaseMobileResponseEntity;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.exception.OutOfSessionException;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.fetcher.investment.rpc.PortfolioRequestBody;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.fetcher.investment.rpc.PortfolioResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.fetcher.transactionalaccount.entities.PendingPaymentsResponseEntity;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.fetcher.transactionalaccount.rpc.AccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.fetcher.transactionalaccount.rpc.PendingPaymentsRequestBody;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.fetcher.transactionalaccount.rpc.PendingPaymentsResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.fetcher.transactionalaccount.rpc.TransactionsRequestBody;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.fetcher.transactionalaccount.rpc.TransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.rpc.BaseResponse;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.form.Form;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

@RequiredArgsConstructor
public class IngApiClient {

    private final TinkHttpClient client;
    private final PersistentStorage persistentStorage;
    private final String aggregator;

    public MobileHelloResponseEntity mobileHello() {
        MobileHelloRequestBody mobileHelloRequestBody = new MobileHelloRequestBody();
        URL url = getUrlWithQueryParams(IngConstants.Urls.MOBILE_HELLO);

        return this.client
                .request(url)
                .post(MobileHelloResponse.class, mobileHelloRequestBody)
                .getMobileResponse();
    }

    public void trustBuilderEnroll(
            String url, String username, String cardNumber, String otp, String deviceId) {
        TrustBuilderRequestBody trustBuilderRequestBody =
                new TrustBuilderRequestBody(username, cardNumber, otp, deviceId, "", true);
        Form.Builder urlEncodedBodyBuilder = Form.builder();
        trustBuilderRequestBody
                .entrySet()
                .forEach(
                        entry ->
                                urlEncodedBodyBuilder.put(entry.getKey(), entry.getValue().get(0)));

        URL trustBuilderUrl = getUrlWithQueryParams(new URL(IngConstants.Urls.HOST + url));

        this.client
                .request(trustBuilderUrl)
                .type(MediaType.APPLICATION_FORM_URLENCODED_TYPE)
                .post(HttpResponse.class, urlEncodedBodyBuilder.build().serialize());
    }

    public HttpResponse trustBuilderLogin(
            String url,
            String ingId,
            String virtualCardNumber,
            int otp,
            String deviceId,
            String psn) {
        TrustBuilderRequestBody trustBuilderRequestBody =
                new TrustBuilderRequestBody(
                        ingId, virtualCardNumber, Integer.toString(otp), deviceId, psn, false);
        URL trustBuilderUrl = getUrlWithQueryParams(new URL(IngConstants.Urls.HOST + url));

        return this.client
                .request(trustBuilderUrl)
                .post(HttpResponse.class, trustBuilderRequestBody);
    }

    public HttpResponse initEnroll(
            String url, String username, String cardNumber, String deviceId) {
        InitEnrollRequestBody initEnrollRequestBody =
                new InitEnrollRequestBody(username, cardNumber, deviceId);
        URL initEnrollUrl = getUrlWithQueryParams(new URL(IngConstants.Urls.HOST + url));

        return this.client.request(initEnrollUrl).post(HttpResponse.class, initEnrollRequestBody);
    }

    public AppCredentialsResponse getAppCredentials(String url, byte[] encryptedQueryData) {
        AppCredentialsBody appCredentialsBody = new AppCredentialsBody(encryptedQueryData);
        URL getAppCredentialsUrl = new URL(IngConstants.Urls.HOST + url);

        return this.client
                .request(getAppCredentialsUrl)
                .post(AppCredentialsResponse.class, appCredentialsBody);
    }

    public PrepareEnrollResponseEntity prepareEnroll(String url) {
        BaseBody prepareEnrollBody = new BaseBody();
        URL prepareEnrollUrl = getUrlWithQueryParams(new URL(IngConstants.Urls.HOST + url));

        return this.client
                .request(prepareEnrollUrl)
                .post(PrepareEnrollResponse.class, prepareEnrollBody)
                .getMobileResponse();
    }

    public BaseMobileResponseEntity confirmEnroll(
            String url,
            String ingId,
            String signingId,
            String challengeResponse,
            int otpSystem,
            String deviceId) {
        ConfirmEnrollRequestBody confirmEnrollRequestBody =
                new ConfirmEnrollRequestBody(
                        ingId,
                        signingId,
                        challengeResponse,
                        Integer.toString(otpSystem),
                        deviceId,
                        aggregator);
        URL confirmEnrollUrl = getUrlWithQueryParams(new URL(IngConstants.Urls.HOST + url));

        return this.client
                .request(confirmEnrollUrl)
                .post(BaseResponse.class, confirmEnrollRequestBody)
                .getMobileResponse();
    }

    public void logout(String url) {
        BaseBody logoutRequestBody = new BaseBody();
        URL logoutUrl = getUrlWithQueryParams(new URL(IngConstants.Urls.HOST + url));

        this.client.request(logoutUrl).post(HttpResponse.class, logoutRequestBody);
    }

    public LoginResponseEntity login(
            String url, String ingId, String virtualCardNumber, String deviceId) {
        LoginRequestBody loginRequestBody =
                new LoginRequestBody(ingId, virtualCardNumber, deviceId);
        URL loginUrl = getUrlWithQueryParams(new URL(IngConstants.Urls.HOST + url));

        try {
            return this.client
                    .request(loginUrl)
                    .post(LoginResponse.class, loginRequestBody)
                    .getMobileResponse();
        } catch (HttpClientException ex) {
            if (ex.getMessage().contains("JsonMappingException")) {
                throw BankServiceError.BANK_SIDE_FAILURE.exception();
            }
            throw ex;
        }
    }

    public HttpResponse getMenuItems() {
        return this.client.request(IngConstants.Urls.MENU_ITEMS).get(HttpResponse.class);
    }

    public Optional<AccountsResponse> fetchAccounts(LoginResponseEntity loginResponse) {
        return getHttpResponseForAccountsFetch(loginResponse).flatMap(this::getAccountsResponse);
    }

    public PortfolioResponse fetchInvestmentPortfolio(URL url, String bbanNumber) {
        PortfolioRequestBody portfolioRequestBody = new PortfolioRequestBody(bbanNumber);
        return client.request(getUrlWithQueryParams(url))
                .post(PortfolioResponse.class, portfolioRequestBody);
    }

    public TransactionsResponse getTransactions(
            String url, String bankIdentifier, int startIndex, int endIndex) {
        TransactionsRequestBody transactionsRequestBody =
                new TransactionsRequestBody(
                        bankIdentifier, Integer.toString(startIndex), Integer.toString(endIndex));

        URL concatenatedUrl = new URL(IngConstants.Urls.BASE_SSO_REQUEST + url);
        URL transactionsUrl = getUrlWithQueryParams(concatenatedUrl);

        return this.client
                .request(transactionsUrl)
                .post(TransactionsResponse.class, transactionsRequestBody);
    }

    public PendingPaymentsResponseEntity getPendingPayments(
            LoginResponseEntity loginResponse, String bankIdentifier) {
        PendingPaymentsRequestBody body = new PendingPaymentsRequestBody(bankIdentifier);

        return loginResponse
                .findPendingPaymentsRequest()
                .map(
                        url ->
                                client.request(getUrlWithQueryParams(url))
                                        .post(PendingPaymentsResponse.class, body)
                                        .getMobileResponse())
                .orElseThrow(
                        () ->
                                new IllegalStateException(
                                        "Could not find pending payments request in list of requests."));
    }

    private Optional<HttpResponse> getHttpResponseForAccountsFetch(
            LoginResponseEntity loginResponse) {
        return loginResponse
                .findAccountRequest()
                .map(
                        accountsUrl ->
                                this.client
                                        .request(getUrlWithQueryParams(accountsUrl))
                                        .post(HttpResponse.class, new BaseBody()))
                .filter(
                        httpResponse ->
                                httpResponse.getHeaders().containsKey(HttpHeaders.CONTENT_TYPE))
                .filter(
                        httpResponse ->
                                httpResponse.getHeaders().get(HttpHeaders.CONTENT_TYPE).size()
                                        == 1);
    }

    private URL getUrlWithQueryParams(URL url) {
        return url.queryParam(
                        IngConstants.Session.ValuePairs.APP_NAME.getKey(),
                        IngConstants.Session.ValuePairs.APP_NAME.getValue())
                .queryParam(
                        IngConstants.Session.ValuePairs.USER_PROFILE.getKey(),
                        IngConstants.Session.ValuePairs.USER_PROFILE.getValue());
    }

    private Optional<AccountsResponse> getAccountsResponse(HttpResponse httpResponse) {
        verifyIsInSession(httpResponse);

        return Optional.of(httpResponse)
                .filter(IngApiClient::isAccountsResponse)
                .map(response -> response.getBody(AccountsResponse.class));
    }

    private void verifyIsInSession(HttpResponse httpResponse) {
        try {
            checkOutOfSession(httpResponse);
        } catch (OutOfSessionException ex) {
            handleOutOfSessionException();
        }
    }

    private void handleOutOfSessionException() {
        this.persistentStorage.put(IngConstants.Storage.IS_MANUAL_AUTHENTICATION, Boolean.TRUE);

        throw BankServiceError.SESSION_TERMINATED.exception();
    }

    private static boolean isAccountsResponse(HttpResponse httpResponse) {
        return hasContentType(httpResponse, MediaType.TEXT_XML);
    }

    private static void checkOutOfSession(HttpResponse httpResponse) throws OutOfSessionException {
        if (hasContentType(httpResponse, MediaType.APPLICATION_JSON)) {
            final BaseResponse baseResponse = httpResponse.getBody(BaseResponse.class);

            baseResponse.getMobileResponse();
        }
    }

    private static boolean hasContentType(HttpResponse httpResponse, String expectedContentType) {
        final String actualContentType =
                httpResponse.getHeaders().get(HttpHeaders.CONTENT_TYPE).get(0).toLowerCase();

        return actualContentType.startsWith(expectedContentType.toLowerCase());
    }
}
