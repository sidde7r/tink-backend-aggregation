package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank;

import com.google.common.base.Strings;
import org.json.JSONObject;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.authenticator.password.rpc.BindDeviceRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.authenticator.password.rpc.BindDeviceResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.authenticator.password.rpc.CheckDeviceResponse;
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
import se.tink.backend.aggregation.nxgen.http.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;

public class DanskeBankApiClient {
    private static final AggregationLogger log = new AggregationLogger(DanskeBankApiClient.class);

    protected final TinkHttpClient client;
    protected final DanskeBankConfiguration configuration;
    private ListAccountsResponse accounts;

    protected DanskeBankApiClient(TinkHttpClient client, DanskeBankConfiguration configuration) {
        this.client = client;
        this.configuration = configuration;
    }

    public void addPersistentHeader(String key, String value) {
        client.addPersistentHeader(key, value);
    }

    public HttpResponse collectDynamicLogonJavascript(String securitySystem, String brand) {
        return client.request(
                        String.format(
                                DanskeBankConstants.Url.DYNAMIC_JS_AUTHENTICATE,
                                securitySystem,
                                brand))
                .header("Referer", configuration.getAppReferer())
                .get(HttpResponse.class);
    }

    private <T> T postRequest(String url, Class<T> responseClazz, Object request) {
        return client.request(url)
                .header("Referer", configuration.getAppReferer())
                .post(responseClazz, request);
    }

    private String postRequest(String url, Object request) {
        return postRequest(url, String.class, request);
    }

    public FinalizeAuthenticationResponse finalizeAuthentication(
            FinalizeAuthenticationRequest request) {
        String response = postRequest(DanskeBankConstants.Url.FINALIZE_AUTHENTICATION, request);

        return DanskeBankDeserializer.convertStringToObject(
                response, FinalizeAuthenticationResponse.class);
    }

    public ListAccountsResponse listAccounts(ListAccountsRequest request) {
        if (accounts == null) {
            accounts =
                    postRequest(
                            DanskeBankConstants.Url.LIST_ACCOUNTS,
                            ListAccountsResponse.class,
                            request);
        }

        return accounts;
    }

    public ListLoansResponse listLoans(ListLoansRequest request) {
        return postRequest(DanskeBankConstants.Url.LIST_LOANS, ListLoansResponse.class, request);
    }

    public LoanDetailsResponse loanDetails(LoanDetailsRequest request) {
        return postRequest(
                DanskeBankConstants.Url.LOAN_DETAILS, LoanDetailsResponse.class, request);
    }

    public ListTransactionsResponse listTransactions(ListTransactionsRequest request) {
        return postRequest(
                DanskeBankConstants.Url.LIST_TRANSACTIONS, ListTransactionsResponse.class, request);
    }

    public FutureTransactionsResponse listUpcomingTransactions(FutureTransactionsRequest request) {
        return postRequest(
                DanskeBankConstants.Url.LIST_UPCOMING_TRANSACTIONS,
                FutureTransactionsResponse.class,
                request);
    }

    public InvestmentAccountsResponse listCustodyAccounts() {
        String response =
                postRequest(
                        DanskeBankConstants.Url.LIST_CUSTODY_ACCOUNTS, new JSONObject().toString());

        return DanskeBankDeserializer.convertStringToObject(
                response, InvestmentAccountsResponse.class);
    }

    public ListSecuritiesResponse listSecurities(ListSecuritiesRequest request) {
        String response = postRequest(DanskeBankConstants.Url.LIST_SECURITIES, request);

        return DanskeBankDeserializer.convertStringToObject(response, ListSecuritiesResponse.class);
    }

    public ListSecurityDetailsResponse listSecurityDetails(ListSecurityDetailsRequest request) {
        String response = postRequest(DanskeBankConstants.Url.LIST_SECURITY_DETAILS, request);

        return DanskeBankDeserializer.convertStringToObject(
                response, ListSecurityDetailsResponse.class);
    }

    public void keepAlive() {
        postRequest(DanskeBankConstants.Url.EXTEND_SESSION, "{}");
    }

    public BindDeviceResponse bindDevice(String stepUpTokenValue, BindDeviceRequest request) {
        RequestBuilder requestBuilder =
                client.request(DanskeBankConstants.Url.DEVICE_BIND_BIND)
                        .header("Referer", configuration.getAppReferer());

        if (!Strings.isNullOrEmpty(stepUpTokenValue)) {
            requestBuilder.header(
                    configuration.getStepUpTokenKey(), stepUpTokenValue.replaceAll("\"", ""));
        }

        String response = requestBuilder.post(String.class, request);

        return DanskeBankDeserializer.convertStringToObject(response, BindDeviceResponse.class);
    }

    public HttpResponse collectDynamicChallengeJavascript() {
        return client.request(DanskeBankConstants.Url.DYNAMIC_JS_AUTHORIZE)
                .header("Referer", configuration.getAppReferer())
                .get(HttpResponse.class);
    }

    public ListOtpResponse listOtpInformation(ListOtpRequest request) {
        String response =
                client.request(DanskeBankConstants.Url.DEVICE_LIST_OTP)
                        .header("Referer", configuration.getAppReferer())
                        .post(String.class, request);

        return DanskeBankDeserializer.convertStringToObject(response, ListOtpResponse.class);
    }

    public CheckDeviceResponse checkDevice(
            String deviceSerialNumberValue, String stepUpTokenValue) {
        RequestBuilder requestBuilder =
                client.request(DanskeBankConstants.Url.DEVICE_BIND_CHECK)
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
            log.info(
                    String.format(
                            "DanskeBank - Found non null error in check device response - response: [%s]",
                            response));
        }

        return checkDeviceResponse;
    }
}
