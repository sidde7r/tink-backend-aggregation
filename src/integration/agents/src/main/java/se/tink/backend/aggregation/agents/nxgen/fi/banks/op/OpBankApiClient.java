package se.tink.backend.aggregation.agents.nxgen.fi.banks.op;

import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.op.OpBankConstants.Headers;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.op.OpBankConstants.IdTags;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.op.OpBankConstants.RequestParameters;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.op.OpBankConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.op.authenticator.rpc.InitRequestEntity;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.op.authenticator.rpc.InitResponseEntity;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.op.authenticator.rpc.OpAuthResponse;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.op.authenticator.rpc.OpBankAdobeAnalyticsResponse;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.op.authenticator.rpc.OpBankAuthenticateCodeRequest;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.op.authenticator.rpc.OpBankAuthenticateResponse;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.op.authenticator.rpc.OpBankConfigurationEntity;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.op.authenticator.rpc.OpBankLoginRequestEntity;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.op.authenticator.rpc.OpBankLoginResponseEntity;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.op.authenticator.rpc.OpBankMobileConfigurationsEntity;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.op.authenticator.rpc.OpBankPostLoginRequest;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.op.authenticator.rpc.OpBankRepTypeResponse;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.op.fetcher.creditcards.rpc.CardsResponse;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.op.fetcher.entities.OpBankPortfolioRootEntity;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.op.fetcher.rpc.CollateralCreditDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.op.fetcher.rpc.CreditDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.op.fetcher.rpc.FetchCreditsResponse;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.op.fetcher.transactionalaccounts.rpc.OpBankAccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.op.fetcher.transactionalaccounts.rpc.OpBankTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.op.rpc.OpBankResponseEntity;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public class OpBankApiClient {

    private final TinkHttpClient client;

    public OpBankApiClient(TinkHttpClient client) {
        this.client = client;
    }

    public InitResponseEntity init() {
        InitRequestEntity requestEntity = new InitRequestEntity();
        return createRequest(Urls.INIT_URI).post(InitResponseEntity.class, requestEntity);
    }

    public OpAuthResponse auth(String authToken) {
        return createRequest(Urls.AUTH)
                .queryParam(RequestParameters.AUTH_TOKEN, authToken)
                .get(OpAuthResponse.class);
    }

    public OpBankResponseEntity instance(String authToken) {
        return createRequest(Urls.INSTANCE)
                .queryParam(RequestParameters.AUTH_TOKEN, authToken)
                .get(OpBankResponseEntity.class);
    }

    public OpBankLoginResponseEntity login(OpBankLoginRequestEntity requestEntity) {
        return createRequest(Urls.LOGIN_URI).post(OpBankLoginResponseEntity.class, requestEntity);
    }

    public OpBankResponseEntity refreshSession() {
        return createRequest(Urls.REFRESH_SESSION_URI).post(OpBankResponseEntity.class);
    }

    public OpBankResponseEntity logout() {
        return createRequest(Urls.LOGOUT_URI).get(OpBankResponseEntity.class);
    }

    public OpBankAccountsResponse fetchAccounts() {
        return createRequest(Urls.ACCOUNTS_URI)
                .queryParam(RequestParameters.FILTER_ALL_PARAM, RequestParameters.FILTER_ALL_VALUE)
                .get(OpBankAccountsResponse.class);
    }

    public OpBankAuthenticateResponse authenticate() {
        return createRequest(Urls.AUTHENTICATE_URI).get(OpBankAuthenticateResponse.class);
    }

    public OpBankAuthenticateResponse authenticate(OpBankAuthenticateCodeRequest request) {
        return createRequest(Urls.AUTHENTICATE_URI).post(OpBankAuthenticateResponse.class, request);
    }

    public OpBankMobileConfigurationsEntity getMobileConfigurations() {
        return createRequest(Urls.CONFIGURATION_URI).get(OpBankMobileConfigurationsEntity.class);
    }

    public OpBankMobileConfigurationsEntity enableExtendedMobileServices(
            String applicationInstanceId) {
        return createRequest(Urls.CONFIGURATION_URI)
                .queryParam(RequestParameters.OVERRIDE_PARAM, RequestParameters.OVERRIDE_VALUE)
                .queryParam(RequestParameters.CREATE_NEW_PARAM, RequestParameters.CREATE_NEW_VALUE)
                .post(
                        OpBankMobileConfigurationsEntity.class,
                        new OpBankConfigurationEntity()
                                .setConfigurations(OpBankConstants.DEFAULT_CONFIGURATIONS)
                                .setApplicationInstanceId(applicationInstanceId)
                                .setConfigurationName(OpBankConstants.DEFAULT_CONFIGURATION_NAME));
    }

    public OpBankTransactionsResponse getTransactions(TransactionalAccount account) {
        return createRequest(
                        Urls.TRANSACTIONS_URL.parameter(
                                OpBankConstants.PARAM_ENCRYPTED_ACCOUNT_NUMBER,
                                account.getApiIdentifier()))
                .queryParam(RequestParameters.MAX_PAST_PARAM, RequestParameters.MAX_PAST_VALUE)
                .get(OpBankTransactionsResponse.class);
    }

    public OpBankTransactionsResponse getTransactions(
            TransactionalAccount account, String previousTransactionId) {
        return createRequest(
                        Urls.TRANSACTIONS_URL.parameter(
                                OpBankConstants.PARAM_ENCRYPTED_ACCOUNT_NUMBER,
                                account.getApiIdentifier()))
                .queryParam(RequestParameters.ENCRYPTED_TRX_ID, previousTransactionId)
                .queryParam(RequestParameters.MAX_FUTURE, RequestParameters.MAX_FUTURE_VALUE)
                .get(OpBankTransactionsResponse.class);
    }

    public void setRepresentationType() {
        createRequest(
                        Urls.REPRESENTATION_TYPE.queryParam(
                                IdTags.REPTYPE_TAG, IdTags.REPTYPE_PERSON_TAG))
                .get(OpBankRepTypeResponse.class);
    }

    public void postLogin(String authToken, String appid) {
        OpBankPostLoginRequest request =
                new OpBankPostLoginRequest().setApplicationInstanceId(appid);
        createRequest(Urls.POSTLOGIN.queryParam(IdTags.AUTH_TOKEN_TAG, authToken)).post(request);
    }

    public OpBankAdobeAnalyticsResponse adobeAnalyticsConfig(
            String authToken, OpBankPersistentStorage persistentStorage) {
        return createRequest(
                        Urls.AUTH_TOKEN_CONFIG
                                .parameter(
                                        IdTags.IDENTIFIER_TAG,
                                        persistentStorage.retrieveInstanceId())
                                .queryParam(IdTags.AUTH_TOKEN_TAG, authToken))
                .get(OpBankAdobeAnalyticsResponse.class);
    }

    public String fetchTradingAssetsSummary() {
        return createRequest(Urls.TRADING_ASSETS_SUMMARY).get(String.class);
    }

    public OpBankPortfolioRootEntity fetchTradingAssetsPortfolios() {
        return createRequest(Urls.TRADING_ASSETS_PORTFOLIOS).get(OpBankPortfolioRootEntity.class);
    }

    public String fetchTradingAssetsPortfolioDetails(String portfolioId) {
        return createRequest(
                        Urls.TRADING_ASSETS_PORTFOLIO_DETAILS
                                .parameter(OpBankConstants.PARAM_NAME_PORTFOLIO_ID, portfolioId)
                                .queryParam(
                                        RequestParameters.TYPE_PARAM, RequestParameters.TYPE_VALUE))
                .get(String.class);
    }

    public CardsResponse fetchCards() {
        return createRequest(Urls.CARDS).get(CardsResponse.class);
    }

    public String fetchCreditCardTransactions(CreditCardAccount account) {
        return getCreditCardTransactionsRequest(account).get(String.class);
    }

    public String fetchCreditCardTransactions(
            CreditCardAccount account, String previousTransactionId) {
        return getCreditCardTransactionsRequest(account)
                .queryParam(RequestParameters.ENCRYPTED_TRX_ID, previousTransactionId)
                .get(String.class);
    }

    public String fetchCreditCardTransactionsOldEndpoint(CreditCardAccount account) {
        return createRequest(
                        Urls.LEGACY_CREDIT_CARD_TRANSACTIONS_URL.parameter(
                                OpBankConstants.PARAM_ENCRYPTED_ACCOUNT_NUMBER,
                                account.getApiIdentifier()))
                .queryParam(RequestParameters.MAX_PAST_PARAM, RequestParameters.MAX_PAST_VALUE)
                .get(String.class);
    }

    private RequestBuilder getCreditCardTransactionsRequest(CreditCardAccount account) {
        return createRequest(
                        Urls.TRANSACTIONS_URL.parameter(
                                OpBankConstants.PARAM_ENCRYPTED_ACCOUNT_NUMBER,
                                account.getApiIdentifier()))
                .queryParam(RequestParameters.MAX_PAST_PARAM, RequestParameters.MAX_PAST_VALUE);
    }

    public FetchCreditsResponse fetchCredits() {
        return createRequest(Urls.CREDITS).get(FetchCreditsResponse.class);
    }

    public String fetchContinuingCreditTransactions(String encryptedAgreementNumber) {
        return createRequest(
                        Urls.CONTINUING_CREDITS_TRANSACTIONS.parameter(
                                OpBankConstants.PARAM_NAME_AGREEMENT_NUMBER_ENCRYPTED,
                                encryptedAgreementNumber))
                .get(String.class);
    }

    public CreditDetailsResponse fetchSpecialCreditDetails(String encryptedAgreementNumber) {
        return createRequest(
                        Urls.SPECIAL_CREDITS_DETAILS.parameter(
                                OpBankConstants.PARAM_NAME_AGREEMENT_NUMBER_ENCRYPTED,
                                encryptedAgreementNumber))
                .get(CreditDetailsResponse.class);
    }

    public CreditDetailsResponse fetchFlexiCreditDetails(String encryptedAgreementNumber) {
        return createRequest(
                        Urls.FLEXI_CREDITS_DETAILS.parameter(
                                OpBankConstants.PARAM_NAME_AGREEMENT_NUMBER_ENCRYPTED,
                                encryptedAgreementNumber))
                .get(CreditDetailsResponse.class);
    }

    public CollateralCreditDetailsResponse fetchCollateralCreditDetails(
            String encryptedAgreementNumber) {
        return createRequest(
                        Urls.COLLATERAL_CREDITS_DETAILS.parameter(
                                OpBankConstants.PARAM_NAME_AGREEMENT_NUMBER_ENCRYPTED,
                                encryptedAgreementNumber))
                .get(CollateralCreditDetailsResponse.class);
    }

    private RequestBuilder createRequest(URL url) {
        return client.request(url)
                .header(Headers.API_VERSION_KEY, Headers.API_VERSION_VALUE)
                .header(Headers.ACCEPT_LANGUAGE, Headers.ACCEPT_LANGUAGE_VALUE)
                .type(MediaType.APPLICATION_JSON_TYPE)
                .accept(MediaType.APPLICATION_JSON_TYPE);
    }
}
