package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.ThirdPartyAppException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.exceptions.errors.ThirdPartyAppError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.BecConstants.Log;
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
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.authenticator.entities.CodeAppScaEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.authenticator.entities.CodeAppTokenEncryptedPayload;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.authenticator.entities.EncryptedPayloadAndroidEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.authenticator.entities.PayloadAndroidEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.authenticator.entities.ScaOptionsEncryptedPayload;
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
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.nemid.error.NemIdError;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.nemid.exception.NemIdException;
import se.tink.backend.aggregation.nxgen.core.account.Account;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.libraries.date.ThreadSafeDateFormat;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class BecApiClient {

    interface NemIdStateConsumer {
        void accept() throws AuthenticationException;
    }

    private Map<String, NemIdStateConsumer> nemIdStateMap = new HashMap<>();

    private BecSecurityHelper securityHelper;
    private final TinkHttpClient apiClient;
    private final BecUrlConfiguration agentUrl;
    private static final AggregationLogger log = new AggregationLogger(BecApiClient.class);

    public BecApiClient(
            BecSecurityHelper securityHelper, TinkHttpClient client, BecUrlConfiguration url) {
        this.securityHelper = securityHelper;
        this.apiClient = client;
        this.agentUrl = url;

        nemIdStateMap.put("1", () -> {});
        nemIdStateMap.put(
                "2",
                () -> {
                    throw ThirdPartyAppError.CANCELLED.exception("NemID was rejected.");
                });
        nemIdStateMap.put(
                "4",
                () -> {
                    throw new NemIdException(NemIdError.TIMEOUT);
                });
        nemIdStateMap = Collections.unmodifiableMap(nemIdStateMap);
    }

    public void appSync() {
        log.info("app sync -> init");

        BaseBecRequest request = baseRequest();

        PayloadAndroidEntity payloadAndroidEntity = new PayloadAndroidEntity();

        payloadAndroidEntity.setAppType(BecConstants.Meta.APP_TYPE);
        payloadAndroidEntity.setAppVersion(BecConstants.Meta.APP_VERSION);
        payloadAndroidEntity.setLocale(BecConstants.Meta.LOCALE);
        payloadAndroidEntity.setOsVersion(BecConstants.Meta.OS_VERSION);
        payloadAndroidEntity.setDeviceType(BecConstants.Meta.DEVICE_TYPE);

        request.setPayload(payloadAndroidEntity);

        createRequest(this.agentUrl.getAppSync())
                .type(MediaType.APPLICATION_JSON_TYPE)
                .post(EncryptedResponse.class, request);
    }

    public ScaOptionsEncryptedPayload scaPrepare(String username, String password)
            throws LoginException {
        try {
            log.info("SCA prepare -> get available options");
            BaseBecRequest request = baseRequest();
            EncryptedPayloadAndroidEntity payloadEntity = scaPrepareRequest(username, password);

            request.setEncryptedPayload(
                    securityHelper.encrypt(
                            SerializationUtils.serializeToString(payloadEntity).getBytes()));
            EncryptedResponse response =
                    createRequest(this.agentUrl.getPrepareSca())
                            .type(MediaType.APPLICATION_JSON_TYPE)
                            .post(EncryptedResponse.class, request);
            String decryptedResponse = securityHelper.decrypt(response.getEncryptedPayload());

            ScaOptionsEncryptedPayload payload =
                    SerializationUtils.deserializeFromString(
                            decryptedResponse, ScaOptionsEncryptedPayload.class);
            log.info(
                    String.format(
                            "SCA prepare -> available login options: %s",
                            payload.getSecondFactorOptions()));
            return payload;
        } catch (BecAuthenticationException e) {
            log.error("SCA prepare -> error get options response: " + e.getMessage());
            throw LoginError.INCORRECT_CREDENTIALS.exception(e.getMessage());
        }
    }

    public CodeAppTokenEncryptedPayload scaPrepare2(String username, String password)
            throws LoginException {
        try {
            log.info("SCA prepare -> get token");
            BaseBecRequest request = baseRequest();
            EncryptedPayloadAndroidEntity payloadEntity = scaPrepare2Request(username, password);
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
            log.error("SCA prepare -> error get token response: " + e.getMessage());
            throw LoginError.INCORRECT_CREDENTIALS.exception(e.getMessage());
        }
    }

    public void pollNemId(String token) throws AuthenticationException {
        log.info("Poll for 2fa prove");
        NemIdPollResponse response =
                createRequest(this.agentUrl.getNemIdPoll())
                        .type(MediaType.APPLICATION_JSON_TYPE)
                        .queryParam("token", token)
                        .get(NemIdPollResponse.class);
        log.info(String.format("The 2fa response: %s", response));

        nemIdStateMap
                .getOrDefault(
                        response.getState(),
                        () -> {
                            throw ThirdPartyAppError.AUTHENTICATION_ERROR.exception(
                                    "Unknown error occured.");
                        })
                .accept();
    }

    public void sca(String username, String password, String token) throws ThirdPartyAppException {
        log.info("SCA -> authenticate");
        try {
            BaseBecRequest request = baseRequest();
            EncryptedPayloadAndroidEntity payloadEntity = scaRequest(username, password, token);
            request.setEncryptedPayload(
                    securityHelper.encrypt(
                            SerializationUtils.serializeToString(payloadEntity).getBytes()));
            createRequest(this.agentUrl.getSca())
                    .type(MediaType.APPLICATION_JSON_TYPE)
                    .post(request);
        } catch (BecAuthenticationException e) {
            log.error("SCA -> error auth response: " + e.getMessage());
            throw ThirdPartyAppError.TIMED_OUT.exception(e.getMessage());
        }
    }

    private EncryptedPayloadAndroidEntity scaRequest(
            String username, String password, String token) {
        EncryptedPayloadAndroidEntity result = scaPrepareRequest(username, password);
        result.setSecondFactor("codeapp");
        result.setCodeapp(new CodeAppScaEntity(token));
        return result;
    }

    private EncryptedPayloadAndroidEntity scaPrepareRequest(String username, String password) {

        EncryptedPayloadAndroidEntity payloadAndroidEntity =
                baseScaPrepareRequest(username, password);
        payloadAndroidEntity.setSecondFactor("default");
        return payloadAndroidEntity;
    }

    private EncryptedPayloadAndroidEntity baseScaPrepareRequest(String username, String password) {
        EncryptedPayloadAndroidEntity payloadAndroidEntity = new EncryptedPayloadAndroidEntity();
        payloadAndroidEntity.setAppType(BecConstants.Meta.APP_TYPE);
        payloadAndroidEntity.setAppVersion(BecConstants.Meta.APP_VERSION);
        payloadAndroidEntity.setLocale(BecConstants.Meta.LOCALE);
        payloadAndroidEntity.setOsVersion(BecConstants.Meta.OS_VERSION);
        payloadAndroidEntity.setDeviceType(BecConstants.Meta.DEVICE_TYPE);
        payloadAndroidEntity.setScreenSize(BecConstants.Meta.SCREEN_SIZE);

        payloadAndroidEntity.setPincode(password);
        payloadAndroidEntity.setUserId(username);

        return payloadAndroidEntity;
    }

    private EncryptedPayloadAndroidEntity scaPrepare2Request(String username, String password) {
        EncryptedPayloadAndroidEntity payloadAndroidEntity =
                baseScaPrepareRequest(username, password);
        payloadAndroidEntity.setSecondFactor("codeapp");
        return payloadAndroidEntity;
    }

    private BaseBecRequest baseRequest() {
        BaseBecRequest request = new BaseBecRequest();
        request.setLabel(BecConstants.Meta.LABEL);
        request.setCipher(BecConstants.Meta.CIPHER);
        request.setKey(securityHelper.getKey());
        return request;
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
            log.errorExtraLong("Could not fetch credit card list", Log.CREDIT_CARD_FETCH_ERROR, ex);
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

    private RequestBuilder createRequest(String url) {
        return this.apiClient
                .request(url)
                .header(BecConstants.Header.PRAGMA_KEY, BecConstants.Header.PRAGMA_VALUE);
    }
}
