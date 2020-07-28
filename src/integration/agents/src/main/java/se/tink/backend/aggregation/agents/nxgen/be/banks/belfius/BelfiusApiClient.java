package se.tink.backend.aggregation.agents.nxgen.be.banks.belfius;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.BelfiusConstants.ExecutionMode;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.BelfiusConstants.Url;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator.rpc.AuthenticateWithCodeRequest;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator.rpc.AuthenticateWithCodeResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator.rpc.CheckStatusRequest;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator.rpc.CheckStatusResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator.rpc.CloseSessionRequest;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator.rpc.FeedStructureRequest;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator.rpc.LoginRequest;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator.rpc.LoginResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator.rpc.OpenSessionRequest;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator.rpc.OpenSessionResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator.rpc.PrepareAuthenticationRequest;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator.rpc.PrepareAuthenticationResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator.rpc.PrepareDeviceRegistrationRequest;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator.rpc.PrepareDeviceRegistrationResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator.rpc.PrepareLoginRequest;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator.rpc.PrepareLoginResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator.rpc.RegisterDeviceRequest;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator.rpc.RegisterDeviceSignResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator.rpc.SendCardNumberResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.fetcher.transactional.rpc.FetchProductsRequest;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.fetcher.transactional.rpc.FetchProductsResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.fetcher.transactional.rpc.FetchTransactionsRequest;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.fetcher.transactional.rpc.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.fetcher.transactional.rpc.FetchUpcomingTransactionsRequest;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.fetcher.transactional.rpc.FetchUpcomingTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.payments.rpc.EntitySelect;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.rpc.BelfiusRequest;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.rpc.BelfiusResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.rpc.MessageResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.rpc.SessionOpenedResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.rpc.StartFlowRequest;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.sessionhandler.rpc.KeepAliveRequest;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.sessionhandler.rpc.TechnicalResponse;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class BelfiusApiClient {

    private final TinkHttpClient client;
    private final BelfiusSessionStorage sessionStorage;
    private final String locale;

    public BelfiusApiClient(
            TinkHttpClient client, BelfiusSessionStorage sessionStorage, String locale) {
        this.client = client;
        this.sessionStorage = sessionStorage;
        this.locale = locale;
    }

    public void requestConfigIos() {
        client.request(Url.CONFIG_IOS)
                .header("Accept", "*/*")
                .header("Accept-Encoding", "br, gzip, deflate")
                .header("Accept-Language", "nl-be")
                .header("Connection", "keep-alive")
                .header("User-Agent", "Belfius%20Mobile/192811614 CFNetwork/974.2.1 Darwin/18.0.0")
                .get(HttpResponse.class);
    }

    public void openSession() {
        this.sessionStorage.clearSessionData();
        SessionOpenedResponse sessionOpenedResponse =
                post(
                                BelfiusConstants.Url.GEPA_RENDERING_URL,
                                OpenSessionResponse.class,
                                OpenSessionRequest.create(this.locale),
                                ExecutionMode.AGGREGATED)
                        .getSessionData();

        this.sessionStorage.putSessionData(
                sessionOpenedResponse.getSessionId(), sessionOpenedResponse.getMachineIdentifier());
    }

    public void openSessionWithMachineIdentifier(String machineIdentifier) {
        this.sessionStorage.clearSessionDataExceptMachineIdentifier();
        SessionOpenedResponse sessionOpenedResponse =
                post(
                                BelfiusConstants.Url.GEPA_RENDERING_URL,
                                OpenSessionResponse.class,
                                OpenSessionRequest.create(this.locale),
                                ExecutionMode.AGGREGATED)
                        .getSessionData();

        this.sessionStorage.putSessionData(sessionOpenedResponse.getSessionId(), machineIdentifier);
    }

    public void keepAlive() {
        post(
                        BelfiusConstants.Url.GEPA_RENDERING_URL,
                        BelfiusResponse.class,
                        KeepAliveRequest.create(),
                        ExecutionMode.AGGREGATED)
                .filter(TechnicalResponse.class)
                .forEach(TechnicalResponse::checkSessionExpired);
    }

    public void startFlow() {
        post(
                BelfiusConstants.Url.GEPA_RENDERING_URL,
                BelfiusResponse.class,
                BelfiusRequest.builder().setRequests(StartFlowRequest.create()),
                ExecutionMode.AGGREGATED);
    }

    public String prepareAuthentication(String panNumber) throws AuthenticationException {
        PrepareAuthenticationResponse response =
                postUserInput(
                        BelfiusConstants.Url.GEPA_RENDERING_URL,
                        PrepareAuthenticationResponse.class,
                        PrepareAuthenticationRequest.create(panNumber),
                        ExecutionMode.AGGREGATED);
        response.validate();
        return response.getChallenge();
    }

    public void authenticateWithCode(String code) throws AuthenticationException {
        AuthenticateWithCodeResponse response =
                postUserInput(
                        BelfiusConstants.Url.GEPA_RENDERING_URL,
                        AuthenticateWithCodeResponse.class,
                        AuthenticateWithCodeRequest.create(code),
                        ExecutionMode.AGGREGATED);
        response.validate();
    }

    public boolean isDeviceRegistered(String panNumber, String deviceTokenHash) {
        return post(
                        BelfiusConstants.Url.GEPA_SERVICE_URL,
                        CheckStatusResponse.class,
                        CheckStatusRequest.create(panNumber, deviceTokenHash),
                        ExecutionMode.SERVICES)
                .isDeviceRegistered();
    }

    public void sendIsDeviceRegistered(String panNumber, String deviceTokenHash) {
        post(
                BelfiusConstants.Url.GEPA_SERVICE_URL,
                CheckStatusResponse.class,
                CheckStatusRequest.create(panNumber, deviceTokenHash),
                ExecutionMode.SERVICES);
    }

    public void consultClientSettings() {
        post(
                BelfiusConstants.Url.GEPA_SERVICE_URL,
                CheckStatusResponse.class,
                CheckStatusRequest.createConsultClientSettings(),
                ExecutionMode.SERVICES);
    }

    public String prepareDeviceRegistration(
            String deviceToken, String deviceBrand, String deviceName) {
        PrepareDeviceRegistrationResponse response =
                post(
                        BelfiusConstants.Url.GEPA_RENDERING_URL,
                        PrepareDeviceRegistrationResponse.class,
                        PrepareDeviceRegistrationRequest.create(
                                deviceToken, deviceBrand, deviceName),
                        ExecutionMode.AGGREGATED);
        return response.getChallenge();
    }

    public BelfiusResponse registerDevice(String signature) throws AuthenticationException {
        RegisterDeviceSignResponse response =
                postUserInput(
                        BelfiusConstants.Url.GEPA_RENDERING_URL,
                        RegisterDeviceSignResponse.class,
                        RegisterDeviceRequest.create(signature),
                        ExecutionMode.AGGREGATED);
        response.validate();
        return response;
    }

    public void closeSession(final String sessionId) {
        post(
                BelfiusConstants.Url.GEPA_RENDERING_URL,
                BelfiusResponse.class,
                CloseSessionRequest.create(sessionId),
                ExecutionMode.AGGREGATED);
    }

    public PrepareLoginResponse prepareLogin(String panNumber) throws LoginException {
        PrepareLoginResponse prepareLoginResponse =
                post(
                        BelfiusConstants.Url.GEPA_RENDERING_URL,
                        PrepareLoginResponse.class,
                        PrepareLoginRequest.create(panNumber),
                        ExecutionMode.AGGREGATED);
        prepareLoginResponse.validate();
        return prepareLoginResponse;
    }

    public LoginResponse login(
            String deviceTokenHashed, String deviceTokenHashedIosComparison, String signature) {

        return postUserInput(
                BelfiusConstants.Url.GEPA_RENDERING_URL,
                LoginResponse.class,
                LoginRequest.create(deviceTokenHashed, deviceTokenHashedIosComparison, signature),
                ExecutionMode.AGGREGATED);
    }

    public LoginResponse loginPw(
            String deviceTokenHashed, String deviceTokenHashedIosComparison, String signature) {

        return postUserInput(
                BelfiusConstants.Url.GEPA_RENDERING_URL,
                LoginResponse.class,
                LoginRequest.createPw(deviceTokenHashed, deviceTokenHashedIosComparison, signature),
                ExecutionMode.AGGREGATED);
    }

    public FetchProductsResponse fetchProducts() {
        return post(
                BelfiusConstants.Url.GEPA_RENDERING_URL,
                FetchProductsResponse.class,
                FetchProductsRequest.create(),
                ExecutionMode.AGGREGATED);
    }

    public void bacProductList() {
        post(
                BelfiusConstants.Url.GEPA_SERVICE_URL,
                BelfiusResponse.class,
                FeedStructureRequest.createBacProductList(),
                ExecutionMode.SERVICES);
    }

    public FetchTransactionsResponse fetchTransactions(String key, boolean initialRequest) {
        BelfiusRequest.Builder requestBuilder =
                initialRequest
                        ? FetchTransactionsRequest.createInitialRequest(key)
                        : FetchTransactionsRequest.createNextPageRequest();

        return post(
                BelfiusConstants.Url.GEPA_RENDERING_URL,
                FetchTransactionsResponse.class,
                requestBuilder,
                ExecutionMode.AGGREGATED);
    }

    public SendCardNumberResponse sendCardNumber(String cardNumber) {
        return post(
                BelfiusConstants.Url.GEPA_RENDERING_URL,
                SendCardNumberResponse.class,
                EntitySelect.createWithCardNumber(sessionStorage.getSessionId(), cardNumber),
                ExecutionMode.AGGREGATED);
    }

    public BelfiusResponse actorInformation() {
        return post(
                Url.GEPA_SERVICE_URL,
                FetchTransactionsResponse.class,
                CheckStatusRequest.createActor(),
                ExecutionMode.SERVICES);
    }

    public FetchUpcomingTransactionsResponse fetchUpcomingTransactions(
            String key, boolean initialRequest) {
        BelfiusRequest.Builder requestBuilder =
                initialRequest
                        ? FetchUpcomingTransactionsRequest.createInitialRequest(key)
                        : FetchUpcomingTransactionsRequest.createNextPageRequest();

        return post(
                BelfiusConstants.Url.GEPA_RENDERING_URL,
                FetchUpcomingTransactionsResponse.class,
                requestBuilder,
                ExecutionMode.AGGREGATED);
    }

    private static String urlEncode(final String string) {
        try {
            return URLEncoder.encode(string, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }
    }

    private <T extends BelfiusResponse> T post(
            URL url,
            Class<T> c,
            BelfiusRequest.Builder builder,
            final ExecutionMode executionMode) {
        setSessionData(builder, executionMode);
        String body =
                "request="
                        + urlEncode(SerializationUtils.serializeToString(builder.build()))
                                .replace("+", "%20");
        HttpResponse httpResponse = buildRequest(url).post(HttpResponse.class, body);
        T response = parseBelfiusResponse(httpResponse, c);
        MessageResponse.validate(response);

        this.sessionStorage.incrementRequestCounter(executionMode);
        return response;
    }

    private <T extends BelfiusResponse> T postUserInput(
            URL url,
            Class<T> c,
            BelfiusRequest.Builder builder,
            final ExecutionMode executionMode) {
        setSessionData(builder, executionMode);
        String body =
                "request="
                        + urlEncode(SerializationUtils.serializeToString(builder.build()))
                                .replace("+", "%20");
        HttpResponse httpResponse = buildRequest(url).post(HttpResponse.class, body);
        T response = parseBelfiusResponse(httpResponse, c);
        this.sessionStorage.incrementRequestCounter(executionMode);
        return response;
    }

    // Note: When Belfius returns a response of error message, sometimes the "Content-Type" is
    // missing, this will
    // result the client interpret the body as binary, which cause exception in getting the body
    // content.
    private <T extends BelfiusResponse> T parseBelfiusResponse(
            HttpResponse httpResponse, Class<T> c) {
        if (httpResponse.getHeaders().getFirst(HttpHeaders.CONTENT_TYPE) == null) {
            return SerializationUtils.deserializeFromString(httpResponse.getBody(String.class), c);
        } else {
            return httpResponse.getBody(c);
        }
    }

    private void setSessionData(BelfiusRequest.Builder builder, final ExecutionMode executionMode) {
        if (this.sessionStorage.containsSessionData()) {
            builder.setSessionId(this.sessionStorage.getSessionId())
                    .setRequestCounter(this.sessionStorage.getRequestCounter(executionMode));
        }
    }

    private RequestBuilder buildRequest(URL url) {
        RequestBuilder builder =
                this.client
                        .request(buildUrl(url))
                        .type(MediaType.APPLICATION_FORM_URLENCODED)
                        .accept(MediaType.WILDCARD);
        addHeaders(builder, BelfiusConstants.HEADERS);
        return builder;
    }

    private URL buildUrl(URL url) {
        return url.parameter(
                BelfiusConstants.UrlParameter.MACHINE_IDENTIFIER,
                this.sessionStorage.getMachineIdentifier());
    }

    private void addHeaders(RequestBuilder builder, Map<String, String> headers) {
        for (Map.Entry<String, String> header : headers.entrySet()) {
            builder.header(header.getKey(), header.getValue());
        }
    }
}
