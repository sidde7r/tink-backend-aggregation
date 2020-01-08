package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.BecConstants.Log;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.accounts.checking.entities.FetchAccountTransactionRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.accounts.checking.rpc.AccountDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.accounts.checking.rpc.FetchAccountResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.accounts.checking.rpc.FetchAccountTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.accounts.creditcard.entities.CardEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.accounts.creditcard.rpc.CardDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.accounts.creditcard.rpc.FetchCardResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.authenticator.BecSecurityHelper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.authenticator.entities.AppSyncAndroidRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.authenticator.entities.EncryptedPayloadAndroidEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.authenticator.entities.PayloadAndroidEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.authenticator.rpc.LoginResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.investment.entities.InstrumentDetailsEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.investment.rpc.DepositDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.investment.rpc.FetchInvestmentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.loan.entities.MortgageLoanEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.loan.rpc.FetchLoanResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.loan.rpc.LoanDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.rpc.BecErrorResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.rpc.FetchUpcomingPaymentsResponse;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.core.account.Account;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.libraries.date.ThreadSafeDateFormat;

public class BecApiClient {

    private static final ObjectMapper mapper = new ObjectMapper();
    private BecSecurityHelper securityHelper;
    private final TinkHttpClient apiClient;
    private final BecUrlConfiguration agentUrl;
    private static final AggregationLogger LOGGER = new AggregationLogger(BecApiClient.class);

    public BecApiClient(
            BecSecurityHelper securityHelper, TinkHttpClient client, BecUrlConfiguration url) {
        this.securityHelper = securityHelper;
        this.apiClient = client;
        this.agentUrl = url;
    }

    public void appSync() {

        AppSyncAndroidRequest request = new AppSyncAndroidRequest();

        PayloadAndroidEntity payloadAndroidEntity = new PayloadAndroidEntity();

        payloadAndroidEntity.setAppType(BecConstants.Meta.APP_TYPE);
        payloadAndroidEntity.setAppVersion(BecConstants.Meta.APP_VERSION);
        payloadAndroidEntity.setLocale(BecConstants.Meta.LOCALE);
        payloadAndroidEntity.setOsVersion(BecConstants.Meta.OS_VERSION);
        payloadAndroidEntity.setDeviceType(BecConstants.Meta.DEVICE_TYPE);

        request.setLabel(BecConstants.Meta.LABEL);
        request.setCipher(BecConstants.Meta.CIPHER);
        request.setKey(securityHelper.getKey());
        request.setPayload(payloadAndroidEntity);

        createRequest(this.agentUrl.getAppSync())
                .type(MediaType.APPLICATION_JSON_TYPE)
                .post(LoginResponse.class, request);
    }

    public void logonChallenge(String username, String password) {
        try {
            EncryptedPayloadAndroidEntity payloadAndroidEntity =
                    new EncryptedPayloadAndroidEntity();
            payloadAndroidEntity.setAppType(BecConstants.Meta.APP_TYPE);
            payloadAndroidEntity.setAppVersion(BecConstants.Meta.APP_VERSION);
            payloadAndroidEntity.setLocale(BecConstants.Meta.LOCALE);
            payloadAndroidEntity.setOsVersion(BecConstants.Meta.OS_VERSION);
            payloadAndroidEntity.setDeviceType(BecConstants.Meta.DEVICE_TYPE);

            payloadAndroidEntity.setBankId(BecConstants.Meta.BANK_ID);
            payloadAndroidEntity.setNemidChallenge("");
            payloadAndroidEntity.setNemidResponse("");
            payloadAndroidEntity.setScreenSize(BecConstants.Meta.SCREEN_SIZE);
            payloadAndroidEntity.setPincode(password);
            payloadAndroidEntity.setUserId(username);

            AppSyncAndroidRequest request = new AppSyncAndroidRequest();
            request.setLabel(BecConstants.Meta.LABEL);
            request.setKey(securityHelper.getKey());
            request.setEncryptedPayload(
                    securityHelper.encrypt(
                            mapper.writeValueAsString(payloadAndroidEntity).getBytes()));
            request.setCipher(BecConstants.Meta.CIPHER);

            createRequest(this.agentUrl.getLoginChallenge())
                    .type(MediaType.APPLICATION_JSON_TYPE)
                    .post(LoginResponse.class, request);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Json processing failed", e);
        }
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
            List<CardEntity> response =
                    createRequest(this.agentUrl.getFetchCard())
                            .queryParam(
                                    BecConstants.Header.QUERY_PARAM_ICONTYPE_KEY,
                                    BecConstants.Header.QUERY_PARAM_ICONTYPE_VALUE)
                            .get(FetchCardResponse.class)
                            .getCardArray();
            return response;
        } catch (HttpResponseException ex) {
            /**
             * Some banks that are part of BEC (such as PFA) throws error (403) when the agent tries
             * to fetch credit cards. We suspect that this happens because those banks actually do
             * not provide credit card service at all. If this is the case, only PFA (and other
             * pension banks) should have this error. We will keep logs and see which banks have
             * issue with credit card
             */
            LOGGER.errorExtraLong(
                    String.format("Could not fetch credit card list"),
                    Log.CREDIT_CARD_FETCH_ERROR,
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

        InstrumentDetailsEntity response =
                createRequest(this.agentUrl.getBaseUrl() + urlDetails)
                        .queryParam(
                                BecConstants.Header.QUERY_PARAM_VERSION_KEY,
                                BecConstants.Header.QUERY_PARAM_FETCH_INSTRUMENTS_VERSION_VALUE)
                        .queryParam(BecConstants.Header.QUERY_PARAM_ACCOUNT_ID_KEY, accountId)
                        .get(InstrumentDetailsEntity.class);
        return response;
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
