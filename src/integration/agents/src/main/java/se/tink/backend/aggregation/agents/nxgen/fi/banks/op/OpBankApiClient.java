package se.tink.backend.aggregation.agents.nxgen.fi.banks.op;

import java.util.Date;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.op.authenticator.rpc.InitRequestEntity;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.op.authenticator.rpc.InitResponseEntity;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.op.authenticator.rpc.OpBankAdobeAnalyticsResponse;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.op.authenticator.rpc.OpBankAuthenticateCodeRequest;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.op.authenticator.rpc.OpBankAuthenticateResponse;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.op.authenticator.rpc.OpBankConfigurationEntity;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.op.authenticator.rpc.OpBankLoginRequestEntity;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.op.authenticator.rpc.OpBankLoginResponseEntity;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.op.authenticator.rpc.OpBankMobileConfigurationsEntity;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.op.authenticator.rpc.OpBankPostLoginRequest;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.op.authenticator.rpc.OpBankRepTypeResponse;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.op.fetcher.transactionalaccounts.rpc.OpBankAccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.op.fetcher.entities.OpBankCardEntity;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.op.fetcher.entities.OpBankPortfolioRootEntity;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.op.fetcher.rpc.CollateralCreditDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.op.fetcher.rpc.CreditDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.op.fetcher.rpc.FetchCardsResponse;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.op.fetcher.rpc.FetchCreditCardTransactionsRequest;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.op.fetcher.rpc.FetchCreditCardTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.op.fetcher.rpc.FetchCreditsResponse;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.op.fetcher.transactionalaccounts.rpc.OpBankTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.op.rpc.OpBankResponseEntity;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.libraries.date.ThreadSafeDateFormat;

/*
 * A class representing the API published by OP Bank used by Tink.
 */
public class OpBankApiClient {

    private final TinkHttpClient client;

    public OpBankApiClient(TinkHttpClient client) {
        this.client = client;
    }

    public InitResponseEntity init(InitRequestEntity requestEntity) {
        return createRequest(OpBankConstants.Urls.INIT_URI)
                .post(InitResponseEntity.class, requestEntity);
    }

    public OpBankLoginResponseEntity login(OpBankLoginRequestEntity requestEntity) {
        return createRequest(OpBankConstants.Urls.LOGIN_URI)
                .post(OpBankLoginResponseEntity.class, requestEntity);
    }

    public OpBankResponseEntity refreshSession() {
        return createRequest(OpBankConstants.Urls.REFRESH_SESSION_URI)
                .post(OpBankResponseEntity.class);
    }

    public OpBankResponseEntity logout() {
        return createRequest(OpBankConstants.Urls.LOGOUT_URI)
                .get(OpBankResponseEntity.class);
    }

    public OpBankAccountsResponse fetchAccounts() {
        return createRequest(OpBankConstants.Urls.ACCOUNTS_URI)
                .queryParam(OpBankConstants.RequestParameters.FILTER_ALL_PARAM,
                        OpBankConstants.RequestParameters.FILTER_ALL_VALUE)
                .get(OpBankAccountsResponse.class);
    }

    public OpBankAuthenticateResponse authenticate() {
        return createRequest(OpBankConstants.Urls.AUTHENTICATE_URI)
                .get(OpBankAuthenticateResponse.class);
    }

    public OpBankAuthenticateResponse authenticate(OpBankAuthenticateCodeRequest request) {
        return createRequest(OpBankConstants.Urls.AUTHENTICATE_URI)
                .post(OpBankAuthenticateResponse.class,
                        request);
    }

    public OpBankMobileConfigurationsEntity getMobileConfigurations() {
        return createRequest(OpBankConstants.Urls.CONFIGURATION_URI)
                .get(OpBankMobileConfigurationsEntity.class);
    }

    public OpBankMobileConfigurationsEntity enableExtendedMobileServices(String applicationInstanceId) {
        return createRequest(OpBankConstants.Urls.CONFIGURATION_URI)
                .queryParam(OpBankConstants.RequestParameters.OVERRIDE_PARAM,
                        OpBankConstants.RequestParameters.OVERRIDE_VALUE)
                .queryParam(OpBankConstants.RequestParameters.CREATE_NEW_PARAM,
                        OpBankConstants.RequestParameters.CREATE_NEW_VALUE)
                .post(OpBankMobileConfigurationsEntity.class,
                        new OpBankConfigurationEntity()
                                .setConfigurations(OpBankConstants.DEFAULT_CONFIGURATIONS)
                                .setApplicationInstanceId(applicationInstanceId)
                                .setConfigurationName(OpBankConstants.DEFAULT_CONFIGURATION_NAME));
    }

    public OpBankTransactionsResponse getTransactions(TransactionalAccount account) {
        return createRequest(OpBankConstants.Urls.TRANSACTIONS_URL
                .parameter(OpBankConstants.PARAM_ENCRYPTED_ACCOUNT_NUMBER, account.getBankIdentifier()))
                .queryParam(OpBankConstants.RequestParameters.MAX_PAST_PARAM,
                        OpBankConstants.RequestParameters.MAX_PAST_VALUE)
                .get(OpBankTransactionsResponse.class);
    }

    public OpBankTransactionsResponse getTransactions(TransactionalAccount account, String previousTransactionId) {
        return createRequest(OpBankConstants.Urls.TRANSACTIONS_URL
                .parameter(OpBankConstants.PARAM_ENCRYPTED_ACCOUNT_NUMBER, account.getBankIdentifier()))
                .queryParam(OpBankConstants.RequestParameters.ENCRYPTED_TRX_ID, previousTransactionId)
                .queryParam(OpBankConstants.RequestParameters.MAX_PAST_PARAM,
                        OpBankConstants.RequestParameters.MAX_PAST_VALUE)
                .get(OpBankTransactionsResponse.class);
    }

    public void setRepresentationType(){
        createRequest(OpBankConstants.Urls.REPRESENTATION_TYPE
                .queryParam(OpBankConstants.IdTags.REPTYPE_TAG, OpBankConstants.IdTags.REPTYPE_PERSON_TAG))
                .get(OpBankRepTypeResponse.class);
    }

    public void postLogin(String authToken, String appid){
        OpBankPostLoginRequest request = new OpBankPostLoginRequest().setApplicationInstanceId(appid);
        createRequest(OpBankConstants.Urls.POSTLOGIN.queryParam(OpBankConstants.IdTags.AUTH_TOKEN_TAG, authToken))
                .post(request);
    }

    public OpBankAdobeAnalyticsResponse adobeAnalyticsConfig(String authToken, OpBankPersistentStorage persistentStorage){
        return createRequest(OpBankConstants.Urls.AUTH_TOKEN_CONFIG
                .parameter(OpBankConstants.IdTags.IDENTIFIER_TAG, persistentStorage.retrieveInstanceId())
                .queryParam(OpBankConstants.IdTags.AUTH_TOKEN_TAG, authToken))
                .get(OpBankAdobeAnalyticsResponse.class);
    }


    public String fetchTradingAssetsSummary() {
        return createRequest(OpBankConstants.Urls.TRADING_ASSETS_SUMMARY)
                .get(String.class);
    }

    public OpBankPortfolioRootEntity fetchTradingAssetsPortfolios() {
        return createRequest(OpBankConstants.Urls.TRADING_ASSETS_PORTFOLIOS)
                .get(OpBankPortfolioRootEntity.class);
    }

    public String fetchTradingAssetsPortfolioDetails(String portfolioId) {
        return createRequest(
                OpBankConstants.Urls.TRADING_ASSETS_PORTFOLIO_DETAILS
                        .parameter(OpBankConstants.PARAM_NAME_PORTFOLIO_ID, portfolioId)
                        .queryParam(OpBankConstants.RequestParameters.TYPE_PARAM,
                                OpBankConstants.RequestParameters.TYPE_VALUE)
                ).get(String.class);
    }

    public FetchCardsResponse fetchCards() {
        return createRequest(OpBankConstants.Urls.CARDS)
                .get(FetchCardsResponse.class);
    }

    public String fetchCardsDetails(String cardNumber, String expiryDate) {
        return createRequest(OpBankConstants.Urls.CARDS_DETAILS
                .parameter(OpBankConstants.PARAM_NAME_CARD_NUMBER, cardNumber)
                .parameter(OpBankConstants.PARAM_NAME_EXPIRY_DATE, expiryDate))
                .get(String.class);
    }

    public FetchCreditCardTransactionsResponse fetchCreditCardTransactions(OpBankCardEntity card, Date fromDate, Date toDate, boolean firstPage) {
        FetchCreditCardTransactionsRequest request = new FetchCreditCardTransactionsRequest()
                .setCardNumber(card.getCardNumber())
                .setCreditAccountNumber(card.getCreditAccountNumber())
                .setExpiryDate(card.getExpiryDate())
                .setNewestTransactionId(card.getNewestTransactionId())
                .setParallelUseCode(card.getParallelUseCode())
                .setProductCode(card.getProductCode())
                .setSolidarityCode(card.getSolidarityCode())
                .setStartDate(formatDate(fromDate));

        if (toDate != null) {
            request.setEndDate(formatDate(toDate));
        }
        return createRequest(OpBankConstants.Urls.CARDS_TRANSACTIONS_URL
                        .queryParam(OpBankConstants.QUERY_STRING_KEY_FIRST_PAGE, String.valueOf(firstPage)))
                .post(FetchCreditCardTransactionsResponse.class, request);
    }

    public FetchCreditsResponse fetchCredits() {
        return createRequest(OpBankConstants.Urls.CREDITS)
                .get(FetchCreditsResponse.class);
    }

    public String fetchContinuingCreditTransactions(String encryptedAgreementNumber) {
        return createRequest(OpBankConstants.Urls.CONTINUING_CREDITS_TRANSACTIONS
                .parameter(OpBankConstants.PARAM_NAME_AGREEMENT_NUMBER_ENCRYPTED, encryptedAgreementNumber))
                .get(String.class);
    }


    public CreditDetailsResponse fetchSpecialCreditDetails(String encryptedAgreementNumber) {
        return createRequest(OpBankConstants.Urls.SPECIAL_CREDITS_DETAILS
                .parameter(OpBankConstants.PARAM_NAME_AGREEMENT_NUMBER_ENCRYPTED, encryptedAgreementNumber))
                .get(CreditDetailsResponse.class);
    }

    public CreditDetailsResponse fetchFlexiCreditDetails(String encryptedAgreementNumber) {
        return createRequest(OpBankConstants.Urls.FLEXI_CREDITS_DETAILS
                .parameter(OpBankConstants.PARAM_NAME_AGREEMENT_NUMBER_ENCRYPTED, encryptedAgreementNumber))
                .get(CreditDetailsResponse.class);
    }


    public CollateralCreditDetailsResponse fetchCollateralCreditDetails(String encryptedAgreementNumber) {
        return createRequest(OpBankConstants.Urls.COLLATERAL_CREDITS_DETAILS
                .parameter(OpBankConstants.PARAM_NAME_AGREEMENT_NUMBER_ENCRYPTED, encryptedAgreementNumber))
                .get(CollateralCreditDetailsResponse.class);
    }


    private RequestBuilder createRequest(URL url) {
        return client
                .request(url)
                .header(OpBankConstants.Headers.API_VERSION_KEY, OpBankConstants.Headers.API_VERSION_VALUE)
                .type(MediaType.APPLICATION_JSON_TYPE)
                .accept(MediaType.APPLICATION_JSON_TYPE);
    }

    private String formatDate(Date date) {
        return ThreadSafeDateFormat.FORMATTER_INTEGER_DATE.format(date);
    }
}
