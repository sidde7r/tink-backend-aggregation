package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.ThirdPartyAppException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.exceptions.errors.ThirdPartyAppError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.BecConstants.ScaOptions;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.accounts.checking.entities.FetchAccountTransactionRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.accounts.checking.rpc.AccountDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.accounts.checking.rpc.FetchAccountResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.accounts.checking.rpc.FetchAccountTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.accounts.creditcard.entities.CardEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.accounts.creditcard.rpc.CardDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.accounts.creditcard.rpc.FetchCardResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.authenticator.BecAuthenticationException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.authenticator.BecSecurityHelper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.authenticator.entities.BaseBecRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.authenticator.entities.CodeAppPayloadScaEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.authenticator.entities.CodeAppScaEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.authenticator.entities.CodeAppTokenEncryptedPayload;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.authenticator.entities.GeneralPayload;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.authenticator.entities.KeyCardChallengeEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.authenticator.entities.KeyCardPayloadScaEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.authenticator.entities.LoggedInEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.authenticator.entities.PayloadAndroidEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.authenticator.entities.ScaOptionsEncryptedPayload;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.authenticator.entities.ScaTokenAuthEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.authenticator.entities.ScaTokenEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.authenticator.entities.SecondFactorOperationsEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.authenticator.rpc.EncryptedResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.authenticator.rpc.NemIdPollResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.investment.entities.InstrumentDetailsEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.investment.rpc.DepositDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.investment.rpc.FetchInvestmentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.loan.entities.MortgageLoanEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.loan.rpc.FetchLoanResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.loan.rpc.LoanDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.rpc.BecErrorResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.rpc.FetchUpcomingPaymentsResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.nemid.exception.NemIdError;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.nemid.exception.NemIdException;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.nemid.exception.NemIdPollTimeoutException;
import se.tink.backend.aggregation.nxgen.core.account.Account;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.libraries.date.ThreadSafeDateFormat;
import se.tink.libraries.i18n.Catalog;
import se.tink.libraries.serialization.utils.SerializationUtils;
import src.integration.nemid.NemIdSupportedLanguageCode;

public class BecApiClient {
    private static final Logger logger =
            LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    interface NemIdStateConsumer {
        void accept() throws AuthenticationException;
    }

    private Map<String, NemIdStateConsumer> nemIdStateMap = new HashMap<>();

    private BecSecurityHelper securityHelper;
    private final TinkHttpClient apiClient;
    private final BecUrlConfiguration agentUrl;
    private final String userNemIdSupportedLanguage;

    public BecApiClient(
            BecSecurityHelper securityHelper,
            TinkHttpClient client,
            BecUrlConfiguration url,
            Catalog catalog) {
        this.securityHelper = securityHelper;
        this.apiClient = client;
        this.agentUrl = url;
        this.userNemIdSupportedLanguage =
                NemIdSupportedLanguageCode.getFromCatalogOrDefault(catalog).getIsoLanguageCode();

        nemIdStateMap.put("1", () -> {});
        nemIdStateMap.put(
                "2",
                () -> {
                    throw new NemIdException(NemIdError.REJECTED);
                });
        nemIdStateMap.put(
                "4",
                () -> {
                    throw new NemIdPollTimeoutException();
                });
        nemIdStateMap = Collections.unmodifiableMap(nemIdStateMap);
    }

    public void appSync() {
        logger.info("app sync -> init");

        BaseBecRequest request = baseRequest();

        PayloadAndroidEntity payloadAndroidEntity = new PayloadAndroidEntity();

        payloadAndroidEntity.setAppType(BecConstants.Meta.APP_TYPE);
        payloadAndroidEntity.setAppVersion(BecConstants.Meta.APP_VERSION);
        payloadAndroidEntity.setLocale(
                NemIdSupportedLanguageCode.DEFAULT_LANGUAGE_CODE.getIsoLanguageCode());
        payloadAndroidEntity.setOsVersion(BecConstants.Meta.OS_VERSION);
        payloadAndroidEntity.setDeviceType(BecConstants.Meta.DEVICE_TYPE);

        request.setPayload(payloadAndroidEntity);

        createRequest(this.agentUrl.getAppSync())
                .type(MediaType.APPLICATION_JSON_TYPE)
                .post(EncryptedResponse.class, request);
    }

    public SecondFactorOperationsEntity postKeyCardValuesAndDecryptResponse(
            String username, String password, String deviceId)
            throws LoginException, NemIdException {
        try {
            BaseBecRequest request = baseRequest();
            GeneralPayload payloadEntity =
                    new GeneralPayload(
                            username,
                            password,
                            deviceId,
                            ScaOptions.KEYCARD_OPTION,
                            userNemIdSupportedLanguage);
            request.setEncryptedPayload(
                    securityHelper.encrypt(
                            SerializationUtils.serializeToString(payloadEntity).getBytes()));
            EncryptedResponse encryptedResponse =
                    createRequest(agentUrl.getPrepareSca())
                            .type(MediaType.APPLICATION_JSON_TYPE)
                            .post(EncryptedResponse.class, request);
            String decryptedResponse =
                    securityHelper.decrypt(encryptedResponse.getEncryptedPayload());
            return SerializationUtils.deserializeFromString(
                    decryptedResponse, SecondFactorOperationsEntity.class);
        } catch (BecAuthenticationException e) {
            throw LoginError.INCORRECT_CHALLENGE_RESPONSE.exception(e.getMessage());
        }
    }

    public LoggedInEntity authCodeApp(
            String username, String password, String token, String deviceId)
            throws ThirdPartyAppException {
        logger.info("SCA -> authenticate");
        try {
            CodeAppPayloadScaEntity payloadEntity =
                    new CodeAppPayloadScaEntity(
                            username,
                            password,
                            deviceId,
                            new CodeAppScaEntity(token),
                            userNemIdSupportedLanguage);

            return logonScaAndDecryptResponse(SerializationUtils.serializeToString(payloadEntity));
        } catch (BecAuthenticationException e) {
            logger.error("SCA CodeApp-> error auth response: {}", e.getMessage(), e);
            throw ThirdPartyAppError.TIMED_OUT.exception(e.getMessage());
        }
    }

    public LoggedInEntity authKeyCard(
            String username,
            String password,
            String challengeResponseValue,
            String nemidChallenge,
            String deviceId)
            throws LoginException, NemIdException {
        try {
            KeyCardPayloadScaEntity payloadEntity =
                    new KeyCardPayloadScaEntity(
                            username,
                            password,
                            deviceId,
                            new KeyCardChallengeEntity(nemidChallenge, challengeResponseValue),
                            userNemIdSupportedLanguage);
            return logonScaAndDecryptResponse(SerializationUtils.serializeToString(payloadEntity));
        } catch (BecAuthenticationException e) {
            logger.error("SCA KeyCard-> error auth response: {}", e.getMessage(), e);
            throw LoginError.INCORRECT_CHALLENGE_RESPONSE.exception(e.getMessage());
        }
    }

    public LoggedInEntity authScaToken(
            String username, String password, String scaToken, String deviceId) {
        try {
            ScaTokenAuthEntity payloadEntity =
                    new ScaTokenAuthEntity(
                            username,
                            password,
                            deviceId,
                            new ScaTokenEntity(scaToken),
                            userNemIdSupportedLanguage);
            return logonScaAndDecryptResponse(SerializationUtils.serializeToString(payloadEntity));
        } catch (BecAuthenticationException e) {
            logger.error("SCA ScaToken-> error auth response: {}", e.getMessage(), e);
            throw LoginError.INCORRECT_CREDENTIALS.exception(e.getMessage());
        }
    }

    public ScaOptionsEncryptedPayload getScaOptions(
            String username, String password, String deviceId)
            throws LoginException, NemIdException {
        logger.info("SCA prepare -> get available options");
        BaseBecRequest request = baseRequest();
        GeneralPayload payloadEntity =
                new GeneralPayload(
                        username,
                        password,
                        deviceId,
                        ScaOptions.DEFAULT_OPTION,
                        userNemIdSupportedLanguage);

        request.setEncryptedPayload(
                securityHelper.encrypt(
                        SerializationUtils.serializeToString(payloadEntity).getBytes()));
        ScaOptionsEncryptedPayload payload = postScaOptionsAndDecryptResponse(request);
        logger.info(
                String.format(
                        "SCA prepare -> available login options: %s",
                        payload.getSecondFactorOptions()));
        return payload;
    }

    public CodeAppTokenEncryptedPayload getNemIdToken(
            String username, String password, String deviceId) throws NemIdException {
        try {
            logger.info("SCA prepare -> get token");
            BaseBecRequest request = baseRequest();
            GeneralPayload payloadEntity =
                    new GeneralPayload(
                            username,
                            password,
                            deviceId,
                            ScaOptions.CODEAPP_OPTION,
                            userNemIdSupportedLanguage);

            request.setEncryptedPayload(
                    securityHelper.encrypt(
                            SerializationUtils.serializeToString(payloadEntity).getBytes()));
            EncryptedResponse response =
                    createRequest(this.agentUrl.getPrepareSca())
                            .type(MediaType.APPLICATION_JSON_TYPE)
                            .post(EncryptedResponse.class, request);
            String decryptedResponse = securityHelper.decrypt(response.getEncryptedPayload());
            return SerializationUtils.deserializeFromString(
                    decryptedResponse, CodeAppTokenEncryptedPayload.class);
        } catch (BecAuthenticationException e) {
            logger.error("SCA prepare -> error get token response: {}", e.getMessage());
            throw new NemIdException(NemIdError.CODEAPP_NOT_REGISTERED);
        }
    }

    public void pollNemId(String token) throws AuthenticationException {
        logger.info("Poll for 2fa approve");
        NemIdPollResponse response =
                createRequest(this.agentUrl.getNemIdPoll())
                        .type(MediaType.APPLICATION_JSON_TYPE)
                        .queryParam("token", token)
                        .get(NemIdPollResponse.class);
        logger.info(String.format("The 2fa response: %s", response));

        nemIdStateMap
                .getOrDefault(
                        response.getState(),
                        () -> {
                            throw ThirdPartyAppError.AUTHENTICATION_ERROR.exception(
                                    "Unknown error occured.");
                        })
                .accept();
    }

    public BecErrorResponse parseBodyAsError(HttpResponse response) {
        return response.getBody(BecErrorResponse.class);
    }

    public FetchAccountResponse fetchAccounts() {
        return createRequest(this.agentUrl.getFetchAccounts()).get(FetchAccountResponse.class);
    }

    public AccountDetailsResponse fetchAccountDetails(String accountId) {
        return createRequest(this.agentUrl.getFetchAccountDetails())
                .queryParam(BecConstants.Url.ACCOUNT_ID_PARAMETER, accountId)
                .type(MediaType.APPLICATION_JSON_TYPE)
                .get(AccountDetailsResponse.class);
    }

    public FetchAccountTransactionsResponse fetchAccountTransactions(
            Account account, Date fromDate, Date toDate) {

        FetchAccountTransactionRequest fetchAccountTransactionRequest =
                new FetchAccountTransactionRequest();

        fetchAccountTransactionRequest.setAccountId(account.getAccountNumber());
        fetchAccountTransactionRequest.setBrowseId("");
        // NOTE: do not have enough records to test out page paginator. set records to 9999 and use
        // date paginator.
        fetchAccountTransactionRequest.setNoOfRecords(9999);
        fetchAccountTransactionRequest.setSearchFromDate(
                ThreadSafeDateFormat.FORMATTER_DAILY.format(fromDate));
        fetchAccountTransactionRequest.setSearchToDate(
                ThreadSafeDateFormat.FORMATTER_DAILY.format(toDate));
        fetchAccountTransactionRequest.setSkipMatched(false);
        fetchAccountTransactionRequest.setSearchText("");

        return createRequest(this.agentUrl.getFetchAccountTransactions())
                .header(BecConstants.Header.PRAGMA_KEY, BecConstants.Header.PRAGMA_VALUE)
                .type(MediaType.APPLICATION_JSON_TYPE)
                .post(FetchAccountTransactionsResponse.class, fetchAccountTransactionRequest);
    }

    public FetchUpcomingPaymentsResponse fetchAccountUpcomingTransactions(Account account) {

        return createRequest(this.agentUrl.getFetchAccountUpcomingTransactions())
                .queryParam(
                        BecConstants.Header.QUERY_PARAM_ACCOUNT_ID_KEY, account.getAccountNumber())
                .queryParam(BecConstants.Header.QUERY_PARAM_BROWSE_ID_KEY, "")
                .queryParam(BecConstants.Header.QUERY_PARAM_NO_DAYS_AHEAD_KEY, "")
                .queryParam(BecConstants.Header.QUERY_PARAM_NO_OF_RECORDS_KEY, "20")
                .queryParam(BecConstants.Header.QUERY_PARAM_SEARCH_FROM_AMOUNT_KEY, "")
                .queryParam(BecConstants.Header.QUERY_PARAM_SEARCH_FROM_DATE_KEY, "")
                .queryParam(BecConstants.Header.QUERY_PARAM_SEARCH_TEXT, "")
                .queryParam(BecConstants.Header.QUERY_PARAM_SEARCH_TO_AMOUNT_KEY, "")
                .queryParam(BecConstants.Header.QUERY_PARAM_SEARCH_TO_DATE_KEY, "")
                .get(FetchUpcomingPaymentsResponse.class);
    }

    public List<CardEntity> fetchCards() {
        try {
            return createRequest(this.agentUrl.getFetchCard())
                    .queryParam(
                            BecConstants.Header.QUERY_PARAM_ICONTYPE_KEY,
                            BecConstants.Header.QUERY_PARAM_ICONTYPE_VALUE)
                    .get(FetchCardResponse.class)
                    .getCardArray();
        } catch (HttpResponseException ex) {
            /*
             * Some banks that are part of BEC (such as PFA) throws error (400) when the agent tries
             * to fetch credit cards. We suspect that this happens because those banks actually do
             * not provide credit card service at all. If this is the case, only PFA (and other
             * pension banks) should have this error. We will keep logs and see which banks have
             * issue with credit card
             */
            logger.error(
                    "tag={} Could not fetch credit card list",
                    BecConstants.Log.CREDIT_CARD_FETCH_ERROR,
                    ex);
            return new ArrayList<>();
        }
    }

    public CardDetailsResponse fetchCardDetails(String urlDetails) {
        return createRequest(this.agentUrl.getBaseUrl() + urlDetails)
                .get(CardDetailsResponse.class);
    }

    public List<MortgageLoanEntity> fetchLoans() {
        return createRequest(this.agentUrl.getFetchLoan())
                .get(FetchLoanResponse.class)
                .getMortgageLoanList();
    }

    public LoanDetailsResponse fetchLoanDetails(String loanNumber) {
        return createRequest(this.agentUrl.getFetchLoanDetails())
                .queryParam(BecConstants.Url.LOAN_NUMBER_PARAMETER, loanNumber)
                .type(MediaType.APPLICATION_JSON_TYPE)
                .get(LoanDetailsResponse.class);
    }

    public FetchInvestmentResponse fetchInvestment() {
        return createRequest(this.agentUrl.getFetchDepot())
                .queryParam(
                        BecConstants.Header.QUERY_PARAM_VERSION_KEY,
                        BecConstants.Header.QUERY_PARAM_VERSION_VALUE)
                .get(FetchInvestmentResponse.class);
    }

    public DepositDetailsResponse fetchDepositDetail(String urlDetails) {
        return createRequest(this.agentUrl.getBaseUrl() + urlDetails)
                .queryParam(
                        BecConstants.Header.QUERY_PARAM_VERSION_KEY,
                        BecConstants.Header.QUERY_PARAM_VERSION_VALUE)
                .get(DepositDetailsResponse.class);
    }

    public InstrumentDetailsEntity fetchInstrumentDetails(String urlDetails, String accountId) {
        return createRequest(this.agentUrl.getBaseUrl() + urlDetails)
                .queryParam(
                        BecConstants.Header.QUERY_PARAM_VERSION_KEY,
                        BecConstants.Header.QUERY_PARAM_FETCH_INSTRUMENTS_VERSION_VALUE)
                .queryParam(BecConstants.Header.QUERY_PARAM_ACCOUNT_ID_KEY, accountId)
                .get(InstrumentDetailsEntity.class);
    }

    public void logout() {
        createRequest(this.agentUrl.getLogout()).type(MediaType.APPLICATION_JSON_TYPE).post();
    }

    private ScaOptionsEncryptedPayload postScaOptionsAndDecryptResponse(BaseBecRequest request)
            throws NemIdException, LoginException {
        try {
            EncryptedResponse response =
                    createRequest(this.agentUrl.getPrepareSca())
                            .type(MediaType.APPLICATION_JSON_TYPE)
                            .post(EncryptedResponse.class, request);
            String decryptedResponse = securityHelper.decrypt(response.getEncryptedPayload());

            return SerializationUtils.deserializeFromString(
                    decryptedResponse, ScaOptionsEncryptedPayload.class);
        } catch (BecAuthenticationException e) {
            logger.error("SCA prepare -> error get options response: " + e.getMessage());
            if (e.getMessage().startsWith("Your chosen PIN code is locked.")) {
                throw new NemIdException(NemIdError.LOCKED_PIN);
            } else if (e.getMessage().startsWith("NemID is blocked. Contact support.")) {
                // This is guessing (!!!) based on similar message when user tries to auth 2fa using
                // method which one does not have registered. So it is possible if no 2fa nemid
                // option is registered it might result in such a message in this place.
                throw new NemIdException(NemIdError.CODEAPP_NOT_REGISTERED);
            } else {
                throw LoginError.INCORRECT_CREDENTIALS.exception(e);
            }
        }
    }

    private BaseBecRequest baseRequest() {
        BaseBecRequest request = new BaseBecRequest();
        request.setLabel(BecConstants.Meta.LABEL);
        request.setCipher(BecConstants.Meta.CIPHER);
        request.setKey(securityHelper.getKey());
        return request;
    }

    private RequestBuilder createRequest(String url) {
        return this.apiClient
                .request(url)
                .header(BecConstants.Header.PRAGMA_KEY, BecConstants.Header.PRAGMA_VALUE);
    }

    private LoggedInEntity logonScaAndDecryptResponse(String serializedEntity) {
        BaseBecRequest request = baseRequest();
        request.setEncryptedPayload(securityHelper.encrypt(serializedEntity.getBytes()));
        EncryptedResponse encryptedResponse =
                createRequest(agentUrl.getSca())
                        .type(MediaType.APPLICATION_JSON_TYPE)
                        .post(EncryptedResponse.class, request);
        String decryptedResponse = securityHelper.decrypt(encryptedResponse.getEncryptedPayload());
        return SerializationUtils.deserializeFromString(decryptedResponse, LoggedInEntity.class);
    }
}
