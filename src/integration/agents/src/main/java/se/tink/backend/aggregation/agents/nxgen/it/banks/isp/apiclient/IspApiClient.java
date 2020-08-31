package se.tink.backend.aggregation.agents.nxgen.it.banks.isp.apiclient;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;
import org.apache.commons.lang.StringUtils;
import se.tink.backend.aggregation.agents.nxgen.it.banks.isp.IspConstants;
import se.tink.backend.aggregation.agents.nxgen.it.banks.isp.IspConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.it.banks.isp.IspConstants.HeaderValues;
import se.tink.backend.aggregation.agents.nxgen.it.banks.isp.authenticator.entity.CheckPinRequestPayload;
import se.tink.backend.aggregation.agents.nxgen.it.banks.isp.authenticator.entity.ConfirmDeviceRequestPayload;
import se.tink.backend.aggregation.agents.nxgen.it.banks.isp.authenticator.entity.RegisterDevice2RequestPayload;
import se.tink.backend.aggregation.agents.nxgen.it.banks.isp.authenticator.entity.RegisterDevice3RequestPayload;
import se.tink.backend.aggregation.agents.nxgen.it.banks.isp.authenticator.entity.RegisterDeviceRequestPayload;
import se.tink.backend.aggregation.agents.nxgen.it.banks.isp.authenticator.rpc.CheckPinRequest;
import se.tink.backend.aggregation.agents.nxgen.it.banks.isp.authenticator.rpc.CheckPinResponse;
import se.tink.backend.aggregation.agents.nxgen.it.banks.isp.authenticator.rpc.CheckTimeRequest;
import se.tink.backend.aggregation.agents.nxgen.it.banks.isp.authenticator.rpc.CheckTimeResponse;
import se.tink.backend.aggregation.agents.nxgen.it.banks.isp.authenticator.rpc.ConfirmDeviceRequest;
import se.tink.backend.aggregation.agents.nxgen.it.banks.isp.authenticator.rpc.ConfirmDeviceResponse;
import se.tink.backend.aggregation.agents.nxgen.it.banks.isp.authenticator.rpc.RegisterDevice2Request;
import se.tink.backend.aggregation.agents.nxgen.it.banks.isp.authenticator.rpc.RegisterDevice2Response;
import se.tink.backend.aggregation.agents.nxgen.it.banks.isp.authenticator.rpc.RegisterDevice3Request;
import se.tink.backend.aggregation.agents.nxgen.it.banks.isp.authenticator.rpc.RegisterDevice3Response;
import se.tink.backend.aggregation.agents.nxgen.it.banks.isp.authenticator.rpc.RegisterDeviceRequest;
import se.tink.backend.aggregation.agents.nxgen.it.banks.isp.authenticator.rpc.RegisterDeviceResponse;
import se.tink.backend.aggregation.agents.nxgen.it.banks.isp.fetcher.entity.TransactionsRequestPayload;
import se.tink.backend.aggregation.agents.nxgen.it.banks.isp.fetcher.rpc.AccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.it.banks.isp.fetcher.rpc.TransactionsRequest;
import se.tink.backend.aggregation.agents.nxgen.it.banks.isp.fetcher.rpc.TransactionsResponse;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class IspApiClient {

    private static final String EMPTY_PAYLOAD_JSON = "{\"payload\": {}}";

    private final SessionStorage sessionStorage;

    private final TinkHttpClient client;

    public IspApiClient(final TinkHttpClient client, final SessionStorage sessionStorage) {
        this.client = Objects.requireNonNull(client);
        this.sessionStorage = Objects.requireNonNull(sessionStorage);
    }

    private RequestBuilder baseAuthenticatedRequest(String endpoint, boolean encrypted) {
        RequestBuilder builder =
                client.request(new URL(IspConstants.BASE_URL + endpoint))
                        .header(HeaderKeys.CYPHER, encrypted)
                        .header(HeaderKeys.LANG, HeaderValues.LANG)
                        .header(HeaderKeys.CLIENT_VERSION, HeaderValues.CLIENT_VERSION)
                        .header(HeaderKeys.CHANNEL, HeaderValues.CHANNEL)
                        .header(HeaderKeys.KEY_ID, HeaderValues.KEY_ID)
                        .header(HeaderKeys.APPLICATION_NAME, HeaderValues.APPLICATION_NAME)
                        .header(HeaderKeys.ROOTED, HeaderValues.ROOTED)
                        .header(HeaderKeys.OPERATION_SYSTEM, HeaderValues.OPERATION_SYSTEM)
                        .header(HeaderKeys.CALLER, HeaderValues.CALLER)
                        .header(HeaderKeys.DEVICE_MODEL, HeaderValues.DEVICE_MODEL)
                        .header(HeaderKeys.ACCESS_MODE, HeaderValues.ACCESS_MODE)
                        .header(HeaderKeys.POWER_SAVING, HeaderValues.POWER_SAVING)
                        .header(HeaderKeys.ACCEPT_ENCODING, HeaderValues.ACCEPT_ENCODING)
                        .header(HeaderKeys.CONTENT, HeaderValues.CONTENT)
                        .header(HeaderKeys.USERAGENT, HeaderValues.USERAGENT)
                        .header(HeaderKeys.USER_AGENT, HeaderValues.USER_AGENT)
                        .header(HeaderKeys.REQUEST_ID, UUID.randomUUID().toString().toUpperCase())
                        .acceptLanguage(HeaderValues.ACCEPT_LANGUAGE)
                        .accept(HeaderValues.ACCEPT);
        String accessToken = sessionStorage.get(IspConstants.StorageKeys.ACCESS_TOKEN);
        if (StringUtils.isNotEmpty(accessToken)) {
            builder =
                    builder.header(
                            HeaderKeys.AUTHORIZATION,
                            String.format(HeaderValues.AUTHORIZATION_PATTERN, accessToken));
        }
        return builder;
    }

    public CheckPinResponse checkPin(String username, String password) {

        return baseAuthenticatedRequest(IspConstants.Endpoints.CHECK_PIN, true)
                .post(
                        CheckPinResponse.class,
                        new CheckPinRequest(new CheckPinRequestPayload(username, password)));
    }

    public RegisterDeviceResponse registerDevice() {
        RegisterDeviceRequestPayload payload = new RegisterDeviceRequestPayload();
        RegisterDeviceRequest request = new RegisterDeviceRequest(payload);
        RegisterDeviceResponse response =
                baseAuthenticatedRequest(IspConstants.Endpoints.REGISTER_DEVICE, true)
                        .post(RegisterDeviceResponse.class, request);
        sessionStorage.put(
                IspConstants.StorageKeys.TRANSACTION_ID, response.getPayload().getTransactionId());
        return response;
    }

    public RegisterDevice2Response registerDevice2(String otp) {
        RegisterDevice2RequestPayload payload =
                new RegisterDevice2RequestPayload(
                        otp, sessionStorage.get(IspConstants.StorageKeys.TRANSACTION_ID));
        RegisterDevice2Request request = new RegisterDevice2Request(payload);
        RegisterDevice2Response response =
                baseAuthenticatedRequest(IspConstants.Endpoints.REGISTER_DEVICE_2, true)
                        .post(RegisterDevice2Response.class, request);
        sessionStorage.put(
                IspConstants.StorageKeys.TRANSACTION_ID, response.getPayload().getTransactionId());
        return response;
    }

    public RegisterDevice3Response registerDevice3(String deviceId, String pin) {
        RegisterDevice3RequestPayload payload =
                new RegisterDevice3RequestPayload(
                        deviceId, pin, sessionStorage.get(IspConstants.StorageKeys.TRANSACTION_ID));
        RegisterDevice3Request request = new RegisterDevice3Request(payload);

        return baseAuthenticatedRequest(IspConstants.Endpoints.REGISTER_DEVICE_3, true)
                .post(RegisterDevice3Response.class, request);
    }

    public CheckTimeResponse checkTime(String username, String deviceId, LocalDateTime deviceTime) {
        CheckTimeRequest request = new CheckTimeRequest(username, deviceId, deviceTime);
        return baseAuthenticatedRequest(IspConstants.Endpoints.CHECK_TIME, true)
                .post(CheckTimeResponse.class, request);
    }

    public ConfirmDeviceResponse confirmDevice(String deviceId, String totp, String transactionId) {
        ConfirmDeviceRequestPayload payload =
                new ConfirmDeviceRequestPayload(deviceId, totp, transactionId);
        ConfirmDeviceRequest request = new ConfirmDeviceRequest(payload);
        return baseAuthenticatedRequest(IspConstants.Endpoints.CONFIRM_DEVICE, true)
                .post(ConfirmDeviceResponse.class, request);
    }

    public AccountsResponse fetchAccounts() {
        return baseAuthenticatedRequest(IspConstants.Endpoints.FETCH_ACCOUNTS, false)
                .post(AccountsResponse.class, EMPTY_PAYLOAD_JSON);
    }

    public TransactionsResponse fetchTransactions(String accountId, LocalDate to, int page) {
        TransactionsRequest transactionsRequest =
                new TransactionsRequest(new TransactionsRequestPayload(accountId, to, page));
        return baseAuthenticatedRequest(IspConstants.Endpoints.FETCH_TRANSACTIONS, false)
                .post(TransactionsResponse.class, transactionsRequest);
    }
}
