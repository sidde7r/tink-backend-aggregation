package se.tink.backend.aggregation.agents.nxgen.be.banks.belfius;

import java.util.Map;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator.rpc.AuthenticateWithCodeRequest;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator.rpc.AuthenticateWithCodeResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator.rpc.CheckStatusRequest;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator.rpc.CheckStatusResponse;
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
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.fetcher.transactional.rpc.FetchProductsRequest;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.fetcher.transactional.rpc.FetchProductsResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.fetcher.transactional.rpc.FetchTransactionsRequest;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.fetcher.transactional.rpc.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.fetcher.transactional.rpc.FetchUpcomingTransactionsRequest;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.fetcher.transactional.rpc.FetchUpcomingTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.payments.entities.BelfiusPaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.payments.entities.getsigningprotocol.SignProtocolResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.payments.entities.preparetransfer.PrepareRoot;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.payments.rpc.AddBeneficiaryRequest;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.payments.rpc.AppRules;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.payments.rpc.BeneficiaryManagementRequest;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.payments.rpc.DocumentSign;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.payments.rpc.DoubleClickPaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.payments.rpc.DoublePaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.payments.rpc.DoubleSignTransferRequest;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.payments.rpc.EntityClick;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.payments.rpc.EntitySelect;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.payments.rpc.GetSigningProtocolRequest;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.payments.rpc.LoadMessages;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.payments.rpc.MenuAccess;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.payments.rpc.PrepareReaderPayment;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.payments.rpc.SecurityType;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.payments.rpc.SignBeneficiaryRequest;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.payments.rpc.SignCounters;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.payments.rpc.SignedPaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.payments.rpc.TransferRequest;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.rpc.BelfiusRequest;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.rpc.BelfiusResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.rpc.MessageResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.rpc.SessionOpenedResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.rpc.StartFlowRequest;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.sessionhandler.rpc.KeepAliveRequest;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.sessionhandler.rpc.TechnicalResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.utils.BelfiusSecurityUtils;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.libraries.serialization.utils.SerializationUtils;
import se.tink.libraries.transfer.rpc.Transfer;

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

    public void openSession() {
        this.sessionStorage.clearSessionData();
        SessionOpenedResponse sessionOpenedResponse =
                post(
                                BelfiusConstants.Url.GEPA_RENDERING_URL,
                                OpenSessionResponse.class,
                                OpenSessionRequest.create(this.locale))
                        .getSessionData();

        this.sessionStorage.putSessionData(
                sessionOpenedResponse.getSessionId(), sessionOpenedResponse.getMachineIdentifier());
    }

    public void keepAlive() {
        post(
                        BelfiusConstants.Url.GEPA_RENDERING_URL,
                        BelfiusResponse.class,
                        KeepAliveRequest.create())
                .filter(TechnicalResponse.class)
                .forEach(TechnicalResponse::checkSessionExpired);
    }

    public void startFlow() {
        post(
                BelfiusConstants.Url.GEPA_RENDERING_URL,
                BelfiusResponse.class,
                BelfiusRequest.builder().setRequests(StartFlowRequest.create()));
    }

    public String prepareAuthentication(String panNumber) throws AuthenticationException {
        PrepareAuthenticationResponse response =
                postUserInput(
                        BelfiusConstants.Url.GEPA_RENDERING_URL,
                        PrepareAuthenticationResponse.class,
                        PrepareAuthenticationRequest.create(panNumber));
        response.validate();
        return response.getChallenge();
    }

    public void authenticateWithCode(String code) throws AuthenticationException {
        AuthenticateWithCodeResponse response =
                postUserInput(
                        BelfiusConstants.Url.GEPA_RENDERING_URL,
                        AuthenticateWithCodeResponse.class,
                        AuthenticateWithCodeRequest.create(code));
        response.validate();
    }

    public boolean isDeviceRegistered(String panNumber, String deviceTokenHash) {
        return post(
                        BelfiusConstants.Url.GEPA_SERVICE_URL,
                        CheckStatusResponse.class,
                        CheckStatusRequest.create(panNumber, deviceTokenHash))
                .isDeviceRegistered();
    }

    public String prepareDeviceRegistration(
            String deviceToken, String deviceBrand, String deviceName) {
        PrepareDeviceRegistrationResponse response =
                post(
                        BelfiusConstants.Url.GEPA_RENDERING_URL,
                        PrepareDeviceRegistrationResponse.class,
                        PrepareDeviceRegistrationRequest.create(
                                deviceToken, deviceBrand, deviceName));
        return response.getChallenge();
    }

    public BelfiusResponse registerDevice(String signature) throws AuthenticationException {
        RegisterDeviceSignResponse response =
                postUserInput(
                        BelfiusConstants.Url.GEPA_RENDERING_URL,
                        RegisterDeviceSignResponse.class,
                        RegisterDeviceRequest.create(signature));
        response.validate();
        return response;
    }

    public PrepareLoginResponse prepareLogin(String panNumber) throws LoginException {
        PrepareLoginResponse prepareLoginResponse =
                post(
                        BelfiusConstants.Url.GEPA_RENDERING_URL,
                        PrepareLoginResponse.class,
                        PrepareLoginRequest.create(panNumber));
        prepareLoginResponse.validate();
        return prepareLoginResponse;
    }

    public LoginResponse login(
            String deviceTokenHashed, String deviceTokenHashedIosComparison, String signature)
            throws AuthenticationException, AuthorizationException {

        LoginResponse loginResponse =
                postUserInput(
                        BelfiusConstants.Url.GEPA_RENDERING_URL,
                        LoginResponse.class,
                        LoginRequest.create(
                                deviceTokenHashed, deviceTokenHashedIosComparison, signature));
        loginResponse.validate();
        return loginResponse;
    }

    public FetchProductsResponse fetchProducts() {
        return post(
                BelfiusConstants.Url.GEPA_RENDERING_URL,
                FetchProductsResponse.class,
                FetchProductsRequest.create());
    }

    public FetchTransactionsResponse fetchTransactions(String key, boolean initialRequest) {
        BelfiusRequest.Builder requestBuilder =
                initialRequest
                        ? FetchTransactionsRequest.createInitialRequest(key)
                        : FetchTransactionsRequest.createNextPageRequest();

        FetchTransactionsResponse transactionsResponse =
                post(
                        BelfiusConstants.Url.GEPA_RENDERING_URL,
                        FetchTransactionsResponse.class,
                        requestBuilder);

        return transactionsResponse;
    }

    public BelfiusResponse loadMessages() {
        return post(
                BelfiusConstants.Url.GEPA_RENDERING_URL,
                FetchTransactionsResponse.class,
                LoadMessages.create(sessionStorage.getSessionId()));
    }

    public BelfiusResponse documentSign() {
        return post(
                BelfiusConstants.Url.GEPA_RENDERING_URL,
                FetchTransactionsResponse.class,
                DocumentSign.create(sessionStorage.getSessionId()));
    }

    public BelfiusResponse appRules() {
        return post(
                BelfiusConstants.Url.GEPA_RENDERING_URL,
                FetchTransactionsResponse.class,
                AppRules.create(sessionStorage.getSessionId()));
    }

    public BelfiusResponse toSignCounters() {
        return post(
                BelfiusConstants.Url.GEPA_RENDERING_URL,
                FetchTransactionsResponse.class,
                SignCounters.create(sessionStorage.getSessionId()));
    }

    public BelfiusResponse menuAccess() {
        return post(
                BelfiusConstants.Url.GEPA_RENDERING_URL,
                FetchTransactionsResponse.class,
                MenuAccess.create(sessionStorage.getSessionId()));
    }

    public BelfiusResponse entitySelect() {
        return post(
                BelfiusConstants.Url.GEPA_RENDERING_URL,
                FetchTransactionsResponse.class,
                EntitySelect.create(sessionStorage.getSessionId()));
    }

    public BelfiusResponse entityClick() {
        return post(
                BelfiusConstants.Url.GEPA_RENDERING_URL,
                FetchTransactionsResponse.class,
                EntityClick.create(sessionStorage.getSessionId()));
    }

    public BelfiusResponse setSecurityType() {
        return post(
                BelfiusConstants.Url.GEPA_RENDERING_URL,
                FetchTransactionsResponse.class,
                SecurityType.create(sessionStorage.getSessionId()));
    }

    public PrepareRoot prepareTransfer() {
        return postTransaction(
                BelfiusConstants.Url.GEPA_RENDERING_URL,
                PrepareRoot.class,
                BeneficiaryManagementRequest.create(sessionStorage.getSessionId()));
    }

    public SignProtocolResponse addBeneficiary(
            Transfer transfer, boolean isStructuredMessage, String name) {
        return postTransaction(
                BelfiusConstants.Url.GEPA_RENDERING_URL,
                SignProtocolResponse.class,
                AddBeneficiaryRequest.create(
                        sessionStorage.getSessionId(), transfer, isStructuredMessage, name));
    }

    public SignProtocolResponse signBeneficiary(String challengeResponse) {
        return postTransaction(
                BelfiusConstants.Url.GEPA_RENDERING_URL,
                SignProtocolResponse.class,
                SignBeneficiaryRequest.create(challengeResponse));
    }

    public BelfiusPaymentResponse executePayment(
            boolean toOwnAccount,
            Transfer transfer,
            String clientHash,
            boolean isStructuredMessage) {
        this.entityClick();
        this.setSecurityType();
        this.entitySelect();
        this.menuAccess();
        this.toSignCounters();
        this.appRules();
        this.documentSign();
        this.fetchProducts();
        this.loadMessages();
        return postTransaction(
                BelfiusConstants.Url.GEPA_RENDERING_URL,
                BelfiusPaymentResponse.class,
                TransferRequest.create(toOwnAccount, transfer, clientHash, isStructuredMessage));
    }

    public SignProtocolResponse doublePayment() {
        return postTransaction(
                BelfiusConstants.Url.GEPA_RENDERING_URL,
                SignProtocolResponse.class,
                DoublePaymentRequest.create());
    }

    public SignProtocolResponse doubleClickPayment() {
        return postTransaction(
                BelfiusConstants.Url.GEPA_RENDERING_URL,
                SignProtocolResponse.class,
                DoubleClickPaymentRequest.create());
    }

    public SignProtocolResponse getSignProtocol() {
        return postTransaction(
                BelfiusConstants.Url.GEPA_RENDERING_URL,
                SignProtocolResponse.class,
                GetSigningProtocolRequest.create(sessionStorage.getSessionId()));
    }

    public SignProtocolResponse getTransferSignChallenge() {
        return postTransaction(
                BelfiusConstants.Url.GEPA_RENDERING_URL,
                SignProtocolResponse.class,
                PrepareReaderPayment.create(sessionStorage.getSessionId()));
    }

    public SignProtocolResponse signTransfer(String challenge) {
        return postTransaction(
                BelfiusConstants.Url.GEPA_RENDERING_URL,
                SignProtocolResponse.class,
                SignedPaymentResponse.create(
                        BelfiusSecurityUtils.generateTransactionId(), challenge));
    }

    public SignProtocolResponse doubleSignTransfer(String challenge) {
        return postTransaction(
                BelfiusConstants.Url.GEPA_RENDERING_URL,
                SignProtocolResponse.class,
                DoubleSignTransferRequest.create(
                        BelfiusSecurityUtils.generateSignTransferId(), challenge));
    }

    public FetchUpcomingTransactionsResponse fetchUpcomingTransactions(
            String key, boolean initialRequest) {
        BelfiusRequest.Builder requestBuilder =
                initialRequest
                        ? FetchUpcomingTransactionsRequest.createInitialRequest(key)
                        : FetchUpcomingTransactionsRequest.createNextPageRequest();

        FetchUpcomingTransactionsResponse transactionsResponse =
                post(
                        BelfiusConstants.Url.GEPA_RENDERING_URL,
                        FetchUpcomingTransactionsResponse.class,
                        requestBuilder);

        return transactionsResponse;
    }

    private <T extends BelfiusResponse> T post(
            URL url, Class<T> c, BelfiusRequest.Builder builder) {
        setSessionData(builder);
        String body = "request=" + SerializationUtils.serializeToString(builder.build());
        HttpResponse httpResponse = buildRequest(url).post(HttpResponse.class, body);
        T response = parseBelfiusResponse(httpResponse, c);
        MessageResponse.validate(response);
        this.sessionStorage.incrementRequestCounter();
        return response;
    }

    private <T extends BelfiusResponse> T postUserInput(
            URL url, Class<T> c, BelfiusRequest.Builder builder) {
        setSessionData(builder);
        String body = "request=" + SerializationUtils.serializeToString(builder.build());
        HttpResponse httpResponse = buildRequest(url).post(HttpResponse.class, body);
        T response = parseBelfiusResponse(httpResponse, c);
        this.sessionStorage.incrementRequestCounter();
        return response;
    }

    private <T extends BelfiusResponse> T postTransaction(
            URL url, Class<T> c, BelfiusRequest.Builder builder) {
        setSessionData(builder);
        String body = "request=" + SerializationUtils.serializeToString(builder.build());
        body = body.replace("\\\\", Character.toString((char) 92));
        HttpResponse httpResponse = buildRequest(url).post(HttpResponse.class, body);
        T response = parseBelfiusResponse(httpResponse, c);
        this.sessionStorage.incrementRequestCounter();
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

    private void setSessionData(BelfiusRequest.Builder builder) {
        if (this.sessionStorage.containsSessionData()) {
            builder.setSessionId(this.sessionStorage.getSessionId())
                    .setRequestCounter(this.sessionStorage.getRequestCounter());
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
