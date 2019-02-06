package se.tink.backend.aggregation.agents.nxgen.mx.banks.bbva;

import com.google.common.base.Strings;
import java.util.Date;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
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
import se.tink.backend.aggregation.agents.nxgen.mx.banks.bbva.fetcher.transactional.rpc.AccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.mx.banks.bbva.fetcher.transactional.rpc.CustomerInfoResponse;
import se.tink.backend.aggregation.agents.nxgen.mx.banks.bbva.fetcher.transactional.rpc.TransactionsResponse;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpClientException;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class BBVAApiClient {
    private final TinkHttpClient client;
    private final PersistentStorage storage;

    public BBVAApiClient(TinkHttpClient client, PersistentStorage storage) {
        this.client = client;
        this.storage = storage;
    }

    private RequestBuilder getGlomoUrl(String resource) {
        return client.request(String.format("%s%s", BBVAConstants.URLS.HOST_GLOMO, resource));
    }

    private RequestBuilder getGlomoRequest(String resource, String contenttype) {
        String deviceId = storage.get(BBVAConstants.STORAGE.DEVICE_IDENTIFIER);

        if (Strings.isNullOrEmpty(deviceId)) {
            throw new IllegalStateException("DeviceId is null or empty!");
        }

        RequestBuilder builder =
                getGlomoUrl(resource)
                        .header(
                                BBVAConstants.HEADERS.DEVICE_SCREEN_SIZE,
                                BBVAConstants.VALUES.DEVICE_SCREEN_SIZE)
                        .header(BBVAConstants.HEADERS.DEVICE_DPI, BBVAConstants.VALUES.DEVICE_DPI)
                        .header(
                                BBVAConstants.HEADERS.DEVICE_OS_VERSION,
                                BBVAConstants.VALUES.DEVICE_OS_VERSION)
                        .header(HttpHeaders.ACCEPT_LANGUAGE, BBVAConstants.VALUES.ACCEPT_LANGUAGE)
                        .header(BBVAConstants.HEADERS.DEVICE_ID, deviceId)
                        .header(
                                BBVAConstants.HEADERS.DEVICE_APP_NAME,
                                BBVAConstants.VALUES.DEVICE_APP_NAME)
                        .header(
                                BBVAConstants.HEADERS.DEVICE_APP_VERSION,
                                BBVAConstants.VALUES.DEVICE_APP_VERSION)
                        .header(HttpHeaders.USER_AGENT, BBVAUtils.getUseragent())
                        .header(
                                BBVAConstants.HEADERS.DEVICE_MODEL_NAME,
                                BBVAConstants.VALUES.DEVICE_MODEL_NAME)
                        .header(
                                BBVAConstants.HEADERS.DEVICE_MODEL_FACTURER,
                                BBVAConstants.VALUES.DEVICE_MODEL_FACTURER)
                        .header(HttpHeaders.CONTENT_TYPE, contenttype)
                        .header(
                                BBVAConstants.HEADERS.DEVICE_OS_NAME,
                                BBVAConstants.VALUES.DEVICE_OS_NAME);

        if (storage.containsKey(BBVAConstants.STORAGE.TSEC)) {
            return builder.header(
                    BBVAConstants.HEADERS.TSEC, storage.get(BBVAConstants.STORAGE.TSEC));
        }
        return builder;
    }

    // AUTH
    public GrantingTicketResponse grantTicket(GrantingTicketRequest request) throws LoginException {
        HttpResponse res = null;
        try {
            res =
                    getGlomoRequest(BBVAConstants.URLS.GRANT_TICKET, MediaType.APPLICATION_JSON)
                            .post(
                                    HttpResponse.class,
                                    SerializationUtils.serializeToString(request));
        } catch (HttpClientException hce) {
            throw LoginError.INCORRECT_CREDENTIALS.exception();
        }

        String tsec = res.getHeaders().getFirst(BBVAConstants.HEADERS.TSEC);
        this.storage.put(BBVAConstants.STORAGE.TSEC, tsec);

        return res.getBody(GrantingTicketResponse.class);
    }

    public ValidateSubscriptionResponse validateSubscription(ValidateSubscriptionRequest request) {
        return getGlomoRequest(BBVAConstants.URLS.VALIDATE_SUBSCRIPTION, MediaType.APPLICATION_JSON)
                .post(
                        ValidateSubscriptionResponse.class,
                        SerializationUtils.serializeToString(request));
    }

    public HttpResponse activateDevice(DeviceActivationRequest request, String boundary) {
        String boundaryWithoutDashes = boundary.substring(2, boundary.length());
        return getGlomoRequest(
                        BBVAConstants.URLS.DEVICE_ACTIVATION,
                        String.format(
                                "%s%s",
                                BBVAConstants.HEADERS.CONTENT_TYPE_MULTIPART,
                                boundaryWithoutDashes))
                .post(HttpResponse.class, BBVAUtils.getActivationDataBoundary(request, boundary));
    }

    public HttpResponse digitalActivation(String customerId, DigitalActivationRequest request) {
        return getGlomoRequest(
                        String.format(BBVAConstants.URLS.DIGITAL_ACTIVATION, customerId),
                        MediaType.APPLICATION_JSON)
                .header(
                        BBVAConstants.HEADERS.AUTHENTICATION_TYPE,
                        BBVAConstants.HEADERS.AUTHENTICATION_TYPE_VALUE)
                .header(
                        BBVAConstants.HEADERS.AUTHENTICATION_DATA,
                        String.format(
                                BBVAConstants.HEADERS.AUTHENTICATION_DATA_DEVICE_ID,
                                storage.get(BBVAConstants.STORAGE.DEVICE_IDENTIFIER)))
                .post(HttpResponse.class, SerializationUtils.serializeToString(request));
    }

    public HttpResponse getContactToken(String phoneNumber) {
        HttpResponse res =
                getGlomoRequest(
                                String.format(BBVAConstants.URLS.CONTACT_TOKEN, phoneNumber),
                                MediaType.APPLICATION_JSON)
                        .get(HttpResponse.class);
        String tsec = res.getHeaders().getFirst(BBVAConstants.HEADERS.TSEC);
        this.storage.put(BBVAConstants.STORAGE.TSEC, tsec);
        return res;
    }

    public TokenAuthCodeResponse getTokenAuthCode(String deviceIdentifier) {
        return getGlomoRequest(BBVAConstants.URLS.TOKEN_AUTH_CODE, MediaType.APPLICATION_JSON)
                .overrideHeader(BBVAConstants.HEADERS.DEVICE_APP_VERSION, "90129")
                .header(HttpHeaders.ACCEPT, MediaType.WILDCARD)
                .post(
                        TokenAuthCodeResponse.class,
                        SerializationUtils.serializeToString(
                                new TokenAuthCodeRequest(deviceIdentifier)));
    }

    public TokenActivationResponse getTokenWithHash(
            String softwareTokenId, String hash, TokenActivationRequest request) {
        String url = String.format(BBVAConstants.URLS.TOKEN_ACTIVATION_HASH, softwareTokenId, hash);
        return getGlomoRequest(url, MediaType.APPLICATION_JSON)
                .put(TokenActivationResponse.class, SerializationUtils.serializeToString(request));
    }

    public HttpResponse registerToken(RegisterTokenRequest registerTokenRequest) {
        return getGlomoRequest(BBVAConstants.URLS.REGISTER_TOKEN, MediaType.APPLICATION_JSON)
                .post(
                        HttpResponse.class,
                        SerializationUtils.serializeToString(registerTokenRequest));
    }

    public HttpResponse updateDevice(String deviceId) {
        return getGlomoRequest(
                        String.format(BBVAConstants.URLS.UPDATE_DEVICE, deviceId),
                        MediaType.APPLICATION_JSON)
                .put(
                        HttpResponse.class,
                        SerializationUtils.serializeToString(new UpdateDeviceRequest()));
    }

    // AIS
    public CustomerInfoResponse getCustomerInfo() {
        HttpResponse response =
                getGlomoRequest(BBVAConstants.URLS.CUSTOMER_INFO, MediaType.APPLICATION_JSON)
                        .header(
                                BBVAConstants.HEADERS.AUTHENTICATION_TYPE,
                                BBVAConstants.HEADERS.AUTHENTICATION_TYPE_VALUE)
                        .header(
                                BBVAConstants.HEADERS.AUTHENTICATION_DATA,
                                String.format(
                                        BBVAConstants.HEADERS.AUTHENTICATION_DATA_DEVICE_ID,
                                        storage.get(BBVAConstants.STORAGE.DEVICE_IDENTIFIER)))
                        .get(HttpResponse.class);
        String tsec = response.getHeaders().getFirst(BBVAConstants.HEADERS.TSEC);
        this.storage.put(BBVAConstants.STORAGE.TSEC, tsec);
        return response.getBody(CustomerInfoResponse.class);
    }

    public AccountsResponse fetchAccounts() {
        return getGlomoRequest(BBVAConstants.URLS.ACCOUNTS, MediaType.APPLICATION_JSON)
                .get(AccountsResponse.class);
    }

    public TransactionsResponse fetchTransactions(String accountId, Date fromDate, Date toDate) {
        return getGlomoRequest(
                        String.format(BBVAConstants.URLS.TRANSACTIONS, accountId),
                        MediaType.APPLICATION_JSON)
                .queryParam(
                        BBVAConstants.QUERY.FROM_DATE,
                        BBVAConstants.DATE.DATE_FORMAT.format(fromDate))
                .queryParam(BBVAConstants.QUERY.PAGE_SIZE, BBVAConstants.QUERY.PAGE_SIZE_VALUE)
                .queryParam(
                        BBVAConstants.QUERY.TO_DATE, BBVAConstants.DATE.DATE_FORMAT.format(toDate))
                .get(TransactionsResponse.class);
    }
}
