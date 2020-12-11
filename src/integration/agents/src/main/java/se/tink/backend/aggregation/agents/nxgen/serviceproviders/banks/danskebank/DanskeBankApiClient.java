package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank;

import com.google.common.base.Strings;
import java.lang.invoke.MethodHandles;
import javax.ws.rs.core.MultivaluedMap;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankConstants.Urls;
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
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.rpc.CardDetailsRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.rpc.CardDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.rpc.CardsListRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.rpc.CardsListResponse;
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
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.libraries.i18n.Catalog;

public class DanskeBankApiClient {
    private static final Logger logger =
            LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final Credentials credentials;
    protected final TinkHttpClient client;
    protected final DanskeBankConfiguration configuration;
    private final Catalog catalog;
    private ListAccountsResponse accounts;

    protected DanskeBankApiClient(
            TinkHttpClient client,
            DanskeBankConfiguration configuration,
            Credentials credentials,
            Catalog catalog) {
        this.client = client;
        this.configuration = configuration;
        this.credentials = credentials;
        this.catalog = catalog;
    }

    public void addPersistentHeader(String key, String value) {
        client.addPersistentHeader(key, value);
    }

    public HttpResponse collectDynamicLogonJavascript(String securitySystem, String brand) {
        return client.request(
                        String.format(
                                DanskeBankConstants.Urls.DYNAMIC_JS_AUTHENTICATE_URL,
                                securitySystem,
                                brand))
                .header(
                        DanskeBankConstants.DanskeRequestHeaders.REFERRER,
                        configuration.getAppReferer())
                .get(HttpResponse.class);
    }

    protected <T> T postRequest(String url, Class<T> responseClazz, Object request) {
        return client.request(url)
                .header(
                        DanskeBankConstants.DanskeRequestHeaders.REFERRER,
                        configuration.getAppReferer())
                .post(responseClazz, request);
    }

    private String postRequest(String url, Object request) {
        return postRequest(url, String.class, request);
    }

    private void handlePersistentAuthHeader(HttpResponse response) {
        MultivaluedMap<String, String> headers = response.getHeaders();
        if (!headers.containsKey(DanskeBankConstants.DanskeRequestHeaders.PERSISTENT_AUTH)) {
            return;
        }

        String persistentAuth =
                headers.getFirst(DanskeBankConstants.DanskeRequestHeaders.PERSISTENT_AUTH);
        if (Strings.isNullOrEmpty(persistentAuth)) {
            return;
        }

        // Store tokens in sensitive payload, so it will be masked from logs
        this.credentials.setSensitivePayload(
                DanskeBankConstants.DanskeRequestHeaders.AUTHORIZATION, persistentAuth);

        this.addPersistentHeader(
                DanskeBankConstants.DanskeRequestHeaders.AUTHORIZATION, persistentAuth);
    }

    public FinalizeAuthenticationResponse finalizeAuthentication(
            FinalizeAuthenticationRequest request) {
        final HttpResponse response =
                postRequest(
                        DanskeBankConstants.Urls.FINALIZE_AUTHENTICATION_URL,
                        HttpResponse.class,
                        request);
        handlePersistentAuthHeader(response);
        String responseBody = response.getBody(String.class);
        return DanskeBankDeserializer.convertStringToObject(
                responseBody, FinalizeAuthenticationResponse.class);
    }

    public ListAccountsResponse listAccounts(ListAccountsRequest request) {
        if (accounts == null) {
            accounts =
                    postRequest(
                            DanskeBankConstants.Urls.LIST_ACCOUNTS_URL,
                            ListAccountsResponse.class,
                            request);
        }

        return accounts;
    }

    /**
     * This method is used only for debug purposes, all data fetched by this method is available in
     * s3 for further analysis.
     *
     * <p>No business output is required from this method, thus it's ok to not care about potential
     * exceptions.
     *
     * @param request request object for cards/list endpoint
     */
    public void listCards(CardsListRequest request) {
        try {
            fetchCardDetails(request);
        } catch (RuntimeException e) {
            logger.warn(e.getMessage());
        }
    }

    private void fetchCardDetails(CardsListRequest request) {
        CardsListResponse cardsListResponse =
                postRequest(Urls.CARDS_LIST_URL, CardsListResponse.class, request);
        cardsListResponse
                .getCards()
                .forEach(
                        cardEntity ->
                                postRequest(
                                        Urls.CARD_DETAILS_URL,
                                        CardDetailsResponse.class,
                                        new CardDetailsRequest(cardEntity.getCardId())));
    }

    public ListLoansResponse listLoans(ListLoansRequest request) {
        return postRequest(
                DanskeBankConstants.Urls.LIST_LOANS_URL, ListLoansResponse.class, request);
    }

    public LoanDetailsResponse loanDetails(LoanDetailsRequest request) {
        return postRequest(
                DanskeBankConstants.Urls.LOAN_DETAILS_URL, LoanDetailsResponse.class, request);
    }

    public ListTransactionsResponse listTransactions(ListTransactionsRequest request) {
        return postRequest(
                DanskeBankConstants.Urls.LIST_TRANSACTIONS_URL,
                ListTransactionsResponse.class,
                request);
    }

    public FutureTransactionsResponse listUpcomingTransactions(FutureTransactionsRequest request) {
        return postRequest(
                DanskeBankConstants.Urls.LIST_UPCOMING_TRANSACTIONS_URL,
                FutureTransactionsResponse.class,
                request);
    }

    public InvestmentAccountsResponse listCustodyAccounts() {
        String response =
                postRequest(
                        DanskeBankConstants.Urls.LIST_CUSTODY_ACCOUNTS_URL,
                        new JSONObject().toString());

        return DanskeBankDeserializer.convertStringToObject(
                response, InvestmentAccountsResponse.class);
    }

    public ListSecuritiesResponse listSecurities(ListSecuritiesRequest request) {
        String response = postRequest(DanskeBankConstants.Urls.LIST_SECURITIES_URL, request);

        return DanskeBankDeserializer.convertStringToObject(response, ListSecuritiesResponse.class);
    }

    public ListSecurityDetailsResponse listSecurityDetails(ListSecurityDetailsRequest request) {
        String response = postRequest(DanskeBankConstants.Urls.LIST_SECURITY_DETAILS_URL, request);

        return DanskeBankDeserializer.convertStringToObject(
                response, ListSecurityDetailsResponse.class);
    }

    public BindDeviceResponse bindDevice(String stepUpTokenValue, BindDeviceRequest request) {
        String secSystem = configuration.getBindDeviceSecuritySystem().orElse("");
        RequestBuilder requestBuilder =
                client.request(DanskeBankConstants.Urls.getDeviceBindBindUrl(secSystem))
                        .header(
                                DanskeBankConstants.DanskeRequestHeaders.REFERRER,
                                configuration.getAppReferer());

        if (!Strings.isNullOrEmpty(stepUpTokenValue)) {
            requestBuilder.header(
                    configuration.getStepUpTokenKey(), stepUpTokenValue.replaceAll("\"", ""));
        }

        HttpResponse response = requestBuilder.post(HttpResponse.class, request);
        // Assign persistent auth header if available.
        handlePersistentAuthHeader(response);

        String responseBody = response.getBody(String.class);
        return DanskeBankDeserializer.convertStringToObject(responseBody, BindDeviceResponse.class);
    }

    public HttpResponse collectDynamicChallengeJavascript() {
        return client.request(DanskeBankConstants.Urls.DYNAMIC_JS_AUTHORIZE_URL)
                .header(
                        DanskeBankConstants.DanskeRequestHeaders.REFERRER,
                        configuration.getAppReferer())
                .get(HttpResponse.class);
    }

    public ListOtpResponse listOtpInformation(ListOtpRequest request) {
        String response =
                client.request(DanskeBankConstants.Urls.DEVICE_LIST_OTP_URL)
                        .header(
                                DanskeBankConstants.DanskeRequestHeaders.REFERRER,
                                configuration.getAppReferer())
                        .post(String.class, request);

        return DanskeBankDeserializer.convertStringToObject(response, ListOtpResponse.class);
    }

    public InitOtpResponse initOtp(String deviceType, String deviceSerialNo) {
        InitOtpRequest request = new InitOtpRequest(deviceType, deviceSerialNo, catalog);

        String response =
                client.request(DanskeBankConstants.Urls.DEVICE_INIT_OTP_URL)
                        .header(
                                DanskeBankConstants.DanskeRequestHeaders.REFERRER,
                                configuration.getAppReferer())
                        .post(String.class, request);

        return DanskeBankDeserializer.convertStringToObject(response, InitOtpResponse.class);
    }

    public CheckDeviceResponse checkDevice(
            String deviceSerialNumberValue, String stepUpTokenValue) {
        RequestBuilder requestBuilder =
                client.request(DanskeBankConstants.Urls.DEVICE_BIND_CHECK_URL)
                        .header(
                                DanskeBankConstants.DanskeRequestHeaders.REFERRER,
                                configuration.getAppReferer())
                        .header(configuration.getDeviceSerialNumberKey(), deviceSerialNumberValue);

        if (!Strings.isNullOrEmpty(stepUpTokenValue)) {
            requestBuilder.header(
                    configuration.getStepUpTokenKey(), stepUpTokenValue.replaceAll("\"", ""));
        }

        String response = requestBuilder.post(String.class, new JSONObject().toString());

        CheckDeviceResponse checkDeviceResponse =
                DanskeBankDeserializer.convertStringToObject(response, CheckDeviceResponse.class);
        if (checkDeviceResponse.getError() != null) {
            logger.info("DanskeBank - Found non null error in check device response");
        }

        return checkDeviceResponse;
    }

    public DanskeIdStatusResponse getStatus(DanskeIdStatusRequest request) {
        return client.request(DanskeBankConstants.Urls.DANSKEID_STATUS)
                .header(
                        DanskeBankConstants.DanskeRequestHeaders.REFERRER,
                        configuration.getAppReferer())
                .post(DanskeIdStatusResponse.class, request);
    }

    public DanskeIdInitResponse danskeIdInit(DanskeIdInitRequest request) {
        return client.request(DanskeBankConstants.Urls.DANSKEID_INIT)
                .header(
                        DanskeBankConstants.DanskeRequestHeaders.REFERRER,
                        configuration.getAppReferer())
                .post(DanskeIdInitResponse.class, request);
    }
}
