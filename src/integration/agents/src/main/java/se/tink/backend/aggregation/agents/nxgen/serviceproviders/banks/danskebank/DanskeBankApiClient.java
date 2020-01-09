package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank;

import com.google.common.base.Strings;
import org.json.JSONObject;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.authenticator.password.rpc.BindDeviceRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.authenticator.password.rpc.BindDeviceResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.authenticator.password.rpc.CheckDeviceResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.authenticator.password.rpc.DanskeIdInitRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.authenticator.password.rpc.DanskeIdInitResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.authenticator.password.rpc.DanskeIdStatusRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.authenticator.password.rpc.DanskeIdStatusResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.authenticator.password.rpc.InitOtpRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.authenticator.password.rpc.InitOtpResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.authenticator.password.rpc.ListOtpRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.authenticator.password.rpc.ListOtpResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.authenticator.rpc.FinalizeAuthenticationRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.authenticator.rpc.FinalizeAuthenticationResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.investment.rpc.InvestmentAccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.investment.rpc.ListSecuritiesRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.investment.rpc.ListSecuritiesResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.investment.rpc.ListSecurityDetailsRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.investment.rpc.ListSecurityDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.rpc.FutureTransactionsRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.rpc.FutureTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.rpc.ListAccountsRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.rpc.ListAccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.rpc.ListLoansRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.rpc.ListLoansResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.rpc.ListTransactionsRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.rpc.ListTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.rpc.LoanDetailsRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.rpc.LoanDetailsResponse;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

public class DanskeBankApiClient {
    private static final AggregationLogger log = new AggregationLogger(DanskeBankApiClient.class);

    protected final TinkHttpClient client;
    protected final DanskeBankConfiguration configuration;
    protected final DanskeBankConstants constants;
    private ListAccountsResponse accounts;

    protected DanskeBankApiClient(TinkHttpClient client, DanskeBankConfiguration configuration) {
        this.client = client;
        this.configuration = configuration;
        /**
         * By default we inject DanskeBankConstants object to use default endpoints. However for DK
         * we need to inject a custom constants object because we want to use a different host
         * (different endpoints). For this reason, we implemented a second constructor which allows
         * us to do so.
         */
        this.constants = new DanskeBankConstants();
    }

    protected DanskeBankApiClient(
            TinkHttpClient client,
            DanskeBankConfiguration configuration,
            DanskeBankConstants constants) {
        this.client = client;
        this.configuration = configuration;
        this.constants = constants;
    }

    public void addPersistentHeader(String key, String value) {
        client.addPersistentHeader(key, value);
    }

    public HttpResponse collectDynamicLogonJavascript(String securitySystem, String brand) {
        return client.request(
                        String.format(
                                constants.getDynamicJsAuthenticateUrl(), securitySystem, brand))
                .header("Referer", configuration.getAppReferer())
                .get(HttpResponse.class);
    }

    protected <T> T postRequest(String url, Class<T> responseClazz, Object request) {
        return client.request(url)
                .header("Referer", configuration.getAppReferer())
                .post(responseClazz, request);
    }

    private String postRequest(String url, Object request) {
        return postRequest(url, String.class, request);
    }

    public FinalizeAuthenticationResponse finalizeAuthentication(
            FinalizeAuthenticationRequest request) throws LoginException {

        String response;

        try {
            response = postRequest(constants.getFinalizeAuthenticationUrl(), request);
        } catch (HttpResponseException e) {
            if (e.getResponse().getStatus() == 401) {
                throw LoginError.INCORRECT_CREDENTIALS.exception(e);
            }
            throw e;
        }

        FinalizeAuthenticationResponse parsedResponse =
                DanskeBankDeserializer.convertStringToObject(
                        response, FinalizeAuthenticationResponse.class);

        return parsedResponse;
    }

    public ListAccountsResponse listAccounts(ListAccountsRequest request) {
        if (accounts == null) {
            accounts =
                    postRequest(
                            constants.getListAccountsUrl(), ListAccountsResponse.class, request);
        }

        return accounts;
    }

    public ListLoansResponse listLoans(ListLoansRequest request) {
        return postRequest(constants.getListLoansUrl(), ListLoansResponse.class, request);
    }

    public LoanDetailsResponse loanDetails(LoanDetailsRequest request) {
        return postRequest(constants.getLoanDetailsUrl(), LoanDetailsResponse.class, request);
    }

    public ListTransactionsResponse listTransactions(ListTransactionsRequest request) {
        return postRequest(
                constants.getListTransactionsUrl(), ListTransactionsResponse.class, request);
    }

    public FutureTransactionsResponse listUpcomingTransactions(FutureTransactionsRequest request) {
        return postRequest(
                constants.getListUpcomingTransactionsUrl(),
                FutureTransactionsResponse.class,
                request);
    }

    public InvestmentAccountsResponse listCustodyAccounts() {
        String response =
                postRequest(constants.getListCustodyAccountsUrl(), new JSONObject().toString());

        return DanskeBankDeserializer.convertStringToObject(
                response, InvestmentAccountsResponse.class);
    }

    public ListSecuritiesResponse listSecurities(ListSecuritiesRequest request) {
        String response = postRequest(constants.getListSecuritiesUrl(), request);

        return DanskeBankDeserializer.convertStringToObject(response, ListSecuritiesResponse.class);
    }

    public ListSecurityDetailsResponse listSecurityDetails(ListSecurityDetailsRequest request) {
        String response = postRequest(constants.getListSecurityDetailsUrl(), request);

        return DanskeBankDeserializer.convertStringToObject(
                response, ListSecurityDetailsResponse.class);
    }

    public void keepAlive() {
        postRequest(constants.getExtendSessionUrl(), "{}");
    }

    public BindDeviceResponse bindDevice(String stepUpTokenValue, BindDeviceRequest request) {
        RequestBuilder requestBuilder =
                client.request(constants.getDeviceBindBindUrl())
                        .header("Referer", configuration.getAppReferer());

        if (!Strings.isNullOrEmpty(stepUpTokenValue)) {
            requestBuilder.header(
                    configuration.getStepUpTokenKey(), stepUpTokenValue.replaceAll("\"", ""));
        }

        String response = requestBuilder.post(String.class, request);

        return DanskeBankDeserializer.convertStringToObject(response, BindDeviceResponse.class);
    }

    public HttpResponse collectDynamicChallengeJavascript() {
        return client.request(constants.getDynamicJsAuthorizeUrl())
                .header("Referer", configuration.getAppReferer())
                .get(HttpResponse.class);
    }

    public ListOtpResponse listOtpInformation(ListOtpRequest request) {
        String response =
                client.request(constants.getDeviceListOtpUrl())
                        .header("Referer", configuration.getAppReferer())
                        .post(String.class, request);

        return DanskeBankDeserializer.convertStringToObject(response, ListOtpResponse.class);
    }

    public InitOtpResponse initOtp(String deviceType, String deviceSerialNo) {
        InitOtpRequest request = new InitOtpRequest(deviceType, deviceSerialNo);

        String response =
                client.request(constants.getDeviceInitOtpUrl())
                        .header("Referer", configuration.getAppReferer())
                        .post(String.class, request);

        return DanskeBankDeserializer.convertStringToObject(response, InitOtpResponse.class);
    }

    public CheckDeviceResponse checkDevice(
            String deviceSerialNumberValue, String stepUpTokenValue) {
        RequestBuilder requestBuilder =
                client.request(constants.getDeviceBindCheckUrl())
                        .header("Referer", configuration.getAppReferer())
                        .header(configuration.getDeviceSerialNumberKey(), deviceSerialNumberValue);

        if (!Strings.isNullOrEmpty(stepUpTokenValue)) {
            requestBuilder.header(
                    configuration.getStepUpTokenKey(), stepUpTokenValue.replaceAll("\"", ""));
        }

        String response = requestBuilder.post(String.class, new JSONObject().toString());

        CheckDeviceResponse checkDeviceResponse =
                DanskeBankDeserializer.convertStringToObject(response, CheckDeviceResponse.class);
        if (checkDeviceResponse.getError() != null) {
            log.info("DanskeBank - Found non null error in check device response");
        }

        return checkDeviceResponse;
    }

    public DanskeIdStatusResponse getStatus(DanskeIdStatusRequest request) {
        return client.request(constants.DANSKEID_STATUS)
                .header(
                        DanskeBankConstants.DanskeRequestHeaders.REFERRER,
                        configuration.getAppReferer())
                .post(DanskeIdStatusResponse.class, request);
    }

    public DanskeIdInitResponse danskeIdInit(DanskeIdInitRequest request) {
        return client.request(constants.DANSKEID_INIT)
                .header(
                        DanskeBankConstants.DanskeRequestHeaders.REFERRER,
                        configuration.getAppReferer())
                .post(DanskeIdInitResponse.class, request);
    }
}
