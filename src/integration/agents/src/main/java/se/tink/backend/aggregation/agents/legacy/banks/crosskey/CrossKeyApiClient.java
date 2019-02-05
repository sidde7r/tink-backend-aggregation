package se.tink.backend.aggregation.agents.banks.crosskey;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import java.net.URI;
import javax.ws.rs.core.MediaType;
import org.apache.http.client.utils.URIBuilder;
import org.joda.time.DateTime;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.banks.crosskey.errors.CrossKeyErrorHandler;
import se.tink.backend.aggregation.agents.banks.crosskey.requests.AddDeviceRequest;
import se.tink.backend.aggregation.agents.banks.crosskey.requests.GenerateTokenRequest;
import se.tink.backend.aggregation.agents.banks.crosskey.requests.PinTanLoginStepPinRequest;
import se.tink.backend.aggregation.agents.banks.crosskey.requests.PinTanLoginStepTanRequest;
import se.tink.backend.aggregation.agents.banks.crosskey.requests.TokenLoginWithConversionRequest;
import se.tink.backend.aggregation.agents.banks.crosskey.responses.AccountsResponse;
import se.tink.backend.aggregation.agents.banks.crosskey.responses.BaseResponse;
import se.tink.backend.aggregation.agents.banks.crosskey.responses.LoanDetailsResponse;
import se.tink.backend.aggregation.agents.banks.crosskey.responses.CrossKeyLoanDetails;
import se.tink.backend.aggregation.agents.banks.crosskey.responses.TransactionsResponse;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.libraries.date.ThreadSafeDateFormat;

public class CrossKeyApiClient {

    private final Client client;
    private CrossKeyErrorHandler errorHandler;

    // URLs
    private String rootUrl;
    private static final String SYSTEM_STATUS_URI = "systemStatus.do";
    private static final String BANK_ID_AUTO_START_LOGIN = "v2/bankIdAutostartLogin.do";
    private static final String BANK_ID_AUTO_START_COLLECT = "v2/bankIdAutostartCollect.do";
    private static final String PIN_TAN_LOGIN_STEP_PIN_URI = "cam/pintanLoginStepPin.do";
    private static final String PIN_TAN_LOGIN_STEP_TAN_URI = "cam/pintanLoginStepTan.do";
    private static final String ADD_DEVICE_URI = "addDevice.do";
    private static final String GENERATE_TOKEN_URI = "generateToken.do";
    private static final String TOKEN_LOGIN_WITH_CONVERSION_URI = "tokenLoginWithConversion.do";
    private static final String ACCOUNTS_URI = "accounts.do";
    private static final String LOAN_DETAILS_URI = "loanDetails.do?loanAccountId=%s";
    private static final String TRANSACTIONS_URI = "transactions.do";
    private static final String LOGOUT_URI = "logout.do";

    // NEW URLs to TEST
    private static final String BANK_ID_AUTO_START_COLLECT_TESTING = "v3/bankIdAutostartCollect.do";
    private static final String ADD_DEVICE_URI_TESTING = "v2/addDevice.do";

    // Parameters
    private String appId;
    private String language;
    private final Credentials credentials;
    private final String userAgent;

    //Constant Values
    private static final String VERSION_1_4_0 = "1.4.0-iOS";

    private final AggregationLogger log;

    public CrossKeyApiClient(Client client, Credentials credentials, AggregationLogger log, String userAgent) {
        this.client = client;
        this.credentials = credentials;
        this.log = log;
        this.userAgent = userAgent;
    }

    public void setErrorHandler(CrossKeyErrorHandler errorHandler) {
        this.errorHandler = errorHandler;
    }

    public void setRootUrl(String url) {
        rootUrl = url;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public BaseResponse systemStatus() throws Exception {
        URI parameters = new URIBuilder()
                .addParameter("appId", appId)
                .addParameter("language", language)
                .build();

        String uri = SYSTEM_STATUS_URI + parameters;

        BaseResponse response = deserializeResponse(
                createClientRequest(uri).get(String.class));

        log.info("Requested system status");

        return response;
    }

    public BaseResponse autoStartBankId() throws Exception {

        BaseResponse response = deserializeResponse(
                createClientRequest(BANK_ID_AUTO_START_LOGIN).post(String.class));

        log.info("Requested bank id auto-start token");

        return response;
    }

    public BaseResponse collectBankId() throws Exception {

        if (appId != null && appId.equalsIgnoreCase(VERSION_1_4_0)) {
            BaseResponse response = deserializeResponse(
                    createClientRequest(BANK_ID_AUTO_START_COLLECT_TESTING).post(String.class));

            log.info("Requested bank id response");

            return response;
        } else {
            BaseResponse response = deserializeResponse(
                    createClientRequest(BANK_ID_AUTO_START_COLLECT).post(String.class));

            log.info("Requested bank id response");

            return response;
        }
    }

    public String getTanPosition() throws Exception {
        PinTanLoginStepPinRequest request = new PinTanLoginStepPinRequest();
        request.setUsername(credentials.getField(Field.Key.USERNAME));
        request.setPassword(credentials.getField(Field.Key.PASSWORD));

        BaseResponse response = deserializeResponse(
                createClientRequest(PIN_TAN_LOGIN_STEP_PIN_URI).post(String.class, request));

        log.info("Identified user");

        return response.getTanPosition();
    }

    public BaseResponse loginWithOneTimeCode(String oneTimeCode) throws Exception {
        PinTanLoginStepTanRequest request = new PinTanLoginStepTanRequest();
        request.setTan(oneTimeCode);

        BaseResponse response = deserializeResponse(
                createClientRequest(PIN_TAN_LOGIN_STEP_TAN_URI).post(String.class, request));

        log.info("Logged in with pin code");

        return response;
    }

    public BaseResponse addDevice(String udId) throws Exception {
        AddDeviceRequest request = new AddDeviceRequest();
        request.setUdId(udId);

        if (appId != null && appId.equalsIgnoreCase(VERSION_1_4_0)) {
            BaseResponse response = deserializeResponse(
                    createClientRequest(ADD_DEVICE_URI_TESTING).post(String.class, request));

            log.info("Added device");

            return response;
        } else {
            BaseResponse response = deserializeResponse(
                    createClientRequest(ADD_DEVICE_URI).post(String.class, request));

            log.info("Added device");

            return response;
        }
    }

    public BaseResponse generateToken(String deviceId) throws Exception {
        GenerateTokenRequest request = new GenerateTokenRequest();
        request.setDeviceId(deviceId);

        BaseResponse response = deserializeResponse(
                createClientRequest(GENERATE_TOKEN_URI).post(String.class, request));

        log.info("Generated deviceToken");

        return response;
    }

    public BaseResponse loginWithToken(String deviceId, String deviceToken) throws Exception {
        TokenLoginWithConversionRequest request = new TokenLoginWithConversionRequest();
        request.setDeviceId(deviceId);
        request.setDeviceToken(deviceToken);
        request.setPassword(credentials.getField(Field.Key.PASSWORD));
        request.setAppVersion(appId);

        BaseResponse response = deserializeResponse(
                createClientRequest(TOKEN_LOGIN_WITH_CONVERSION_URI).post(String.class, request));

        log.info("Logged in with deviceToken");

        return response;
    }

    public AccountsResponse getAccounts() throws Exception {
        URI parameters = new URIBuilder()
                .addParameter("showHidden", "true")
                .build();

        String uri = ACCOUNTS_URI + parameters;

        AccountsResponse response = deserializeResponse(
                createClientRequest(uri).get(String.class),
                AccountsResponse.class);

        log.info("Fetched " + response.getAccounts().size() + " accounts");

        return response;
    }

    public CrossKeyLoanDetails getLoanDetails(String accountId) throws Exception {
        String uri = String.format(LOAN_DETAILS_URI, accountId);
        LoanDetailsResponse response = deserializeResponse(
                createClientRequest(uri).get(String.class),
                LoanDetailsResponse.class);
        return response.getLoanDetailsVO();
    }

    public TransactionsResponse getTransactionsFor(String accountId, DateTime fromDate, DateTime toDate)
            throws Exception {
        URI parameters = new URIBuilder()
                .addParameter("accountId", accountId)
                .addParameter("fromdate", fromDate != null ?
                        ThreadSafeDateFormat.FORMATTER_INTEGER_DATE.format(fromDate.toDate()) :
                        "")
                .addParameter("todate", toDate != null ?
                        ThreadSafeDateFormat.FORMATTER_INTEGER_DATE.format(toDate.toDate()) :
                        "")
                .build();

        String uri = TRANSACTIONS_URI + parameters;

        TransactionsResponse response = deserializeResponse(
                createClientRequest(uri).get(String.class),
                TransactionsResponse.class);

        if (response.getTransactions().size() == 0) {
            log.info("No more transactions to fetch from the account");
        } else {
            log.info("Fetched " + response.getTransactions().size() + " transactions");
        }

        return response;
    }

    public void logout() throws Exception {
        createClientRequest(LOGOUT_URI).post();
        log.info("Logged out");
    }

    private BaseResponse deserializeResponse(String response) throws Exception {
        return BaseResponse.deserializeResponse(response, errorHandler);
    }

    private <T extends BaseResponse> T deserializeResponse(String response, Class<T> model) throws Exception {
        return BaseResponse.deserializeResponse(response, model, errorHandler);
    }

    private WebResource.Builder createClientRequest(String uri) {
        return client.resource(rootUrl + uri).header("User-Agent", userAgent)
                .type(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON_TYPE);
    }
}
