package se.tink.backend.aggregation.agents.nxgen.mx.banks.bbva;

import com.google.common.base.Strings;
import java.util.Date;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.nxgen.mx.banks.bbva.authenticator.rpc.DeviceActivationRequest;
import se.tink.backend.aggregation.agents.nxgen.mx.banks.bbva.authenticator.rpc.DigitalActivationRequest;
import se.tink.backend.aggregation.agents.nxgen.mx.banks.bbva.authenticator.rpc.GrantingTicketRequest;
import se.tink.backend.aggregation.agents.nxgen.mx.banks.bbva.authenticator.rpc.GrantingTicketResponse;
import se.tink.backend.aggregation.agents.nxgen.mx.banks.bbva.authenticator.rpc.RegisterTokenRequest;
import se.tink.backend.aggregation.agents.nxgen.mx.banks.bbva.authenticator.rpc.TokenActivationRequest;
import se.tink.backend.aggregation.agents.nxgen.mx.banks.bbva.authenticator.rpc.TokenActivationResponse;
import se.tink.backend.aggregation.agents.nxgen.mx.banks.bbva.authenticator.rpc.TokenAuthCodeRequest;
import se.tink.backend.aggregation.agents.nxgen.mx.banks.bbva.authenticator.rpc.TokenAuthCodeResponse;
import se.tink.backend.aggregation.agents.nxgen.mx.banks.bbva.authenticator.rpc.UpdateDeviceRequest;
import se.tink.backend.aggregation.agents.nxgen.mx.banks.bbva.authenticator.rpc.ValidateSubscriptionRequest;
import se.tink.backend.aggregation.agents.nxgen.mx.banks.bbva.authenticator.rpc.ValidateSubscriptionResponse;
import se.tink.backend.aggregation.agents.nxgen.mx.banks.bbva.fetcher.credit.rpc.CreditResponse;
import se.tink.backend.aggregation.agents.nxgen.mx.banks.bbva.fetcher.credit.rpc.CreditTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.mx.banks.bbva.fetcher.loan.rpc.LoanResponse;
import se.tink.backend.aggregation.agents.nxgen.mx.banks.bbva.fetcher.transactional.rpc.AccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.mx.banks.bbva.fetcher.transactional.rpc.CustomerInfoResponse;
import se.tink.backend.aggregation.agents.nxgen.mx.banks.bbva.fetcher.transactional.rpc.TransactionsResponse;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class BbvaMxApiClient {
    private final TinkHttpClient client;
    private final PersistentStorage storage;

    public BbvaMxApiClient(TinkHttpClient client, PersistentStorage storage) {
        this.client = client;
        this.storage = storage;
    }

    private RequestBuilder getGlomoUrl(String resource) {
        return client.request(String.format("%s%s", BbvaMxConstants.URLS.HOST_GLOMO, resource));
    }

    private RequestBuilder getGlomoRequest(String resource, String contenttype) {
        String deviceId = storage.get(BbvaMxConstants.STORAGE.DEVICE_IDENTIFIER);

        if (Strings.isNullOrEmpty(deviceId)) {
            throw new IllegalStateException("DeviceId is null or empty!");
        }

        RequestBuilder builder =
                getGlomoUrl(resource)
                        .header(
                                BbvaMxConstants.HEADERS.DEVICE_SCREEN_SIZE,
                                BbvaMxConstants.VALUES.DEVICE_SCREEN_SIZE)
                        .header(
                                BbvaMxConstants.HEADERS.DEVICE_DPI,
                                BbvaMxConstants.VALUES.DEVICE_DPI)
                        .header(
                                BbvaMxConstants.HEADERS.DEVICE_OS_VERSION,
                                BbvaMxConstants.VALUES.DEVICE_OS_VERSION)
                        .header(HttpHeaders.ACCEPT_LANGUAGE, BbvaMxConstants.VALUES.ACCEPT_LANGUAGE)
                        .header(BbvaMxConstants.HEADERS.DEVICE_ID, deviceId)
                        .header(
                                BbvaMxConstants.HEADERS.DEVICE_APP_NAME,
                                BbvaMxConstants.VALUES.DEVICE_APP_NAME)
                        .header(
                                BbvaMxConstants.HEADERS.DEVICE_APP_VERSION,
                                BbvaMxConstants.VALUES.DEVICE_APP_VERSION)
                        .header(HttpHeaders.USER_AGENT, BbvaMxUtils.getUseragent())
                        .header(
                                BbvaMxConstants.HEADERS.DEVICE_MODEL_NAME,
                                BbvaMxConstants.VALUES.DEVICE_MODEL_NAME)
                        .header(
                                BbvaMxConstants.HEADERS.DEVICE_MODEL_FACTURER,
                                BbvaMxConstants.VALUES.DEVICE_MODEL_FACTURER)
                        .header(HttpHeaders.CONTENT_TYPE, contenttype)
                        .header(
                                BbvaMxConstants.HEADERS.DEVICE_OS_NAME,
                                BbvaMxConstants.VALUES.DEVICE_OS_NAME);

        if (storage.containsKey(BbvaMxConstants.STORAGE.TSEC)) {
            return builder.header(
                    BbvaMxConstants.HEADERS.TSEC, storage.get(BbvaMxConstants.STORAGE.TSEC));
        }
        return builder;
    }

    // AUTH
    public GrantingTicketResponse grantTicket(GrantingTicketRequest request) {
        HttpResponse res =
                getGlomoRequest(BbvaMxConstants.URLS.GRANT_TICKET, MediaType.APPLICATION_JSON)
                        .post(HttpResponse.class, SerializationUtils.serializeToString(request));

        String tsec = res.getHeaders().getFirst(BbvaMxConstants.HEADERS.TSEC);
        this.storage.put(BbvaMxConstants.STORAGE.TSEC, tsec);

        return res.getBody(GrantingTicketResponse.class);
    }

    public ValidateSubscriptionResponse validateSubscription(ValidateSubscriptionRequest request) {
        return getGlomoRequest(
                        BbvaMxConstants.URLS.VALIDATE_SUBSCRIPTION, MediaType.APPLICATION_JSON)
                .post(
                        ValidateSubscriptionResponse.class,
                        SerializationUtils.serializeToString(request));
    }

    public HttpResponse activateDevice(DeviceActivationRequest request, String boundary) {
        String boundaryWithoutDashes = boundary.substring(2, boundary.length());
        String contentType =
                String.format(
                        BbvaMxConstants.HEADERS.CONTENT_TYPE_MULTIPART, boundaryWithoutDashes);
        return getGlomoRequest(BbvaMxConstants.URLS.DEVICE_ACTIVATION, contentType)
                .post(HttpResponse.class, BbvaMxUtils.getActivationDataBoundary(request, boundary));
    }

    public HttpResponse digitalActivation(String customerId, DigitalActivationRequest request) {
        String resource = String.format(BbvaMxConstants.URLS.DIGITAL_ACTIVATION, customerId);
        String authenticationDataValue =
                String.format(
                        BbvaMxConstants.HEADERS.AUTHENTICATION_DATA_DEVICE_ID,
                        storage.get(BbvaMxConstants.STORAGE.DEVICE_IDENTIFIER));
        return getGlomoRequest(resource, MediaType.APPLICATION_JSON)
                .header(
                        BbvaMxConstants.HEADERS.AUTHENTICATION_TYPE,
                        BbvaMxConstants.HEADERS.AUTHENTICATION_TYPE_VALUE)
                .header(BbvaMxConstants.HEADERS.AUTHENTICATION_DATA, authenticationDataValue)
                .post(HttpResponse.class, SerializationUtils.serializeToString(request));
    }

    public HttpResponse getContactToken(String phoneNumber) {
        String resource = String.format(BbvaMxConstants.URLS.CONTACT_TOKEN, phoneNumber);
        HttpResponse res =
                getGlomoRequest(resource, MediaType.APPLICATION_JSON).get(HttpResponse.class);
        String tsec = res.getHeaders().getFirst(BbvaMxConstants.HEADERS.TSEC);
        this.storage.put(BbvaMxConstants.STORAGE.TSEC, tsec);
        return res;
    }

    public TokenAuthCodeResponse getTokenAuthCode(String deviceIdentifier) {
        return getGlomoRequest(BbvaMxConstants.URLS.TOKEN_AUTH_CODE, MediaType.APPLICATION_JSON)
                .overrideHeader(BbvaMxConstants.HEADERS.DEVICE_APP_VERSION, "90129")
                .header(HttpHeaders.ACCEPT, MediaType.WILDCARD)
                .post(
                        TokenAuthCodeResponse.class,
                        SerializationUtils.serializeToString(
                                new TokenAuthCodeRequest(deviceIdentifier)));
    }

    public TokenActivationResponse getTokenWithHash(
            String softwareTokenId, String hash, TokenActivationRequest request) {
        String resource =
                String.format(BbvaMxConstants.URLS.TOKEN_ACTIVATION_HASH, softwareTokenId, hash);
        return getGlomoRequest(resource, MediaType.APPLICATION_JSON)
                .put(TokenActivationResponse.class, SerializationUtils.serializeToString(request));
    }

    public HttpResponse registerToken(RegisterTokenRequest registerTokenRequest) {
        return getGlomoRequest(BbvaMxConstants.URLS.REGISTER_TOKEN, MediaType.APPLICATION_JSON)
                .post(
                        HttpResponse.class,
                        SerializationUtils.serializeToString(registerTokenRequest));
    }

    public HttpResponse updateDevice(String deviceId) {
        String resource = String.format(BbvaMxConstants.URLS.UPDATE_DEVICE, deviceId);
        return getGlomoRequest(resource, MediaType.APPLICATION_JSON)
                .put(
                        HttpResponse.class,
                        SerializationUtils.serializeToString(new UpdateDeviceRequest()));
    }

    // AIS
    public CustomerInfoResponse getCustomerInfo() {
        String authenticationData =
                String.format(
                        BbvaMxConstants.HEADERS.AUTHENTICATION_DATA_DEVICE_ID,
                        storage.get(BbvaMxConstants.STORAGE.DEVICE_IDENTIFIER));
        HttpResponse response =
                getGlomoRequest(BbvaMxConstants.URLS.CUSTOMER_INFO, MediaType.APPLICATION_JSON)
                        .header(
                                BbvaMxConstants.HEADERS.AUTHENTICATION_TYPE,
                                BbvaMxConstants.HEADERS.AUTHENTICATION_TYPE_VALUE)
                        .header(BbvaMxConstants.HEADERS.AUTHENTICATION_DATA, authenticationData)
                        .get(HttpResponse.class);
        String tsec = response.getHeaders().getFirst(BbvaMxConstants.HEADERS.TSEC);
        this.storage.put(BbvaMxConstants.STORAGE.TSEC, tsec);
        return response.getBody(CustomerInfoResponse.class);
    }

    public AccountsResponse fetchAccounts() {
        return getGlomoRequest(BbvaMxConstants.URLS.ACCOUNTS, MediaType.APPLICATION_JSON)
                .get(AccountsResponse.class);
    }

    public TransactionsResponse fetchTransactions(String accountId, Date fromDate, Date toDate) {
        String resource = String.format(BbvaMxConstants.URLS.TRANSACTIONS, accountId);
        return getGlomoRequest(resource, MediaType.APPLICATION_JSON)
                .queryParam(
                        BbvaMxConstants.QUERY.FROM_DATE,
                        BbvaMxConstants.DATE.DATE_FORMAT.format(fromDate))
                .queryParam(BbvaMxConstants.QUERY.PAGE_SIZE, BbvaMxConstants.QUERY.PAGE_SIZE_VALUE)
                .queryParam(
                        BbvaMxConstants.QUERY.TO_DATE,
                        BbvaMxConstants.DATE.DATE_FORMAT.format(toDate))
                .get(TransactionsResponse.class);
    }

    public CreditTransactionsResponse fetchCreditTransactions(
            String accountId, Date fromDate, Date toDate) {
        String resource = String.format(BbvaMxConstants.URLS.CREDIT_TRANSACTIONS, accountId);
        return getGlomoRequest(resource, MediaType.APPLICATION_JSON)
                .queryParam(
                        BbvaMxConstants.QUERY.FROM_DATE,
                        BbvaMxConstants.DATE.DATE_FORMAT.format(fromDate))
                .queryParam(BbvaMxConstants.QUERY.PAGE_SIZE, BbvaMxConstants.QUERY.PAGE_SIZE_VALUE)
                .queryParam(
                        BbvaMxConstants.QUERY.TO_DATE,
                        BbvaMxConstants.DATE.DATE_FORMAT.format(toDate))
                .get(CreditTransactionsResponse.class);
    }

    public CreditResponse fetchCredit() {
        return getGlomoRequest(BbvaMxConstants.URLS.CREDIT, MediaType.APPLICATION_JSON)
                .get(CreditResponse.class);
    }

    public LoanResponse fetchLoans() {
        return getGlomoRequest(BbvaMxConstants.URLS.LOANS, MediaType.APPLICATION_JSON)
                .get(LoanResponse.class);
    }
}
