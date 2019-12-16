package se.tink.backend.aggregation.agents.nxgen.it.banks.isp;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import org.apache.commons.lang.StringUtils;
import se.tink.backend.aggregation.agents.nxgen.it.banks.isp.IspConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.it.banks.isp.IspConstants.HeaderValues;
import se.tink.backend.aggregation.agents.nxgen.it.banks.isp.entity.CheckPinRequestPayload;
import se.tink.backend.aggregation.agents.nxgen.it.banks.isp.entity.ConfirmDeviceRequestPayload;
import se.tink.backend.aggregation.agents.nxgen.it.banks.isp.entity.RegisterDevice2RequestPayload;
import se.tink.backend.aggregation.agents.nxgen.it.banks.isp.entity.RegisterDevice3RequestPayload;
import se.tink.backend.aggregation.agents.nxgen.it.banks.isp.entity.RegisterDeviceRequestPayload;
import se.tink.backend.aggregation.agents.nxgen.it.banks.isp.rpc.BaseResponse;
import se.tink.backend.aggregation.agents.nxgen.it.banks.isp.rpc.CheckPinRequest;
import se.tink.backend.aggregation.agents.nxgen.it.banks.isp.rpc.CheckPinResponse;
import se.tink.backend.aggregation.agents.nxgen.it.banks.isp.rpc.CheckTimeRequest;
import se.tink.backend.aggregation.agents.nxgen.it.banks.isp.rpc.CheckTimeResponse;
import se.tink.backend.aggregation.agents.nxgen.it.banks.isp.rpc.ConfirmDeviceRequest;
import se.tink.backend.aggregation.agents.nxgen.it.banks.isp.rpc.ConfirmDeviceResponse;
import se.tink.backend.aggregation.agents.nxgen.it.banks.isp.rpc.RegisterDevice2Request;
import se.tink.backend.aggregation.agents.nxgen.it.banks.isp.rpc.RegisterDevice2Response;
import se.tink.backend.aggregation.agents.nxgen.it.banks.isp.rpc.RegisterDevice3Request;
import se.tink.backend.aggregation.agents.nxgen.it.banks.isp.rpc.RegisterDevice3Response;
import se.tink.backend.aggregation.agents.nxgen.it.banks.isp.rpc.RegisterDeviceRequest;
import se.tink.backend.aggregation.agents.nxgen.it.banks.isp.rpc.RegisterDeviceResponse;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class IspApiClient {

    private static final String TRANSACTION_ID_STORAGE_KEY = "TRX_ID";
    private static final String EMPTY_PAYLOAD_JSON = "\"payload\": {}";
    private static final String NULL_PAYLOAD_JSON = "\"payload\": null";

    private final SessionStorage sessionStorage;
    private final PersistentStorage persistentStorage;

    private final TinkHttpClient client;

    public IspApiClient(
            final TinkHttpClient client,
            final SessionStorage sessionStorage,
            final PersistentStorage persistentStorage) {
        this.client = Objects.requireNonNull(client);
        this.sessionStorage = Objects.requireNonNull(sessionStorage);
        this.persistentStorage = Objects.requireNonNull(persistentStorage);
    }

    private RequestBuilder baseRequest(String endpoint) {
        RequestBuilder builder =
                client.request(new URL(IspConstants.BASE_URL + endpoint))
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
        String accessToken = sessionStorage.get(IspAuthenticator.SESSION_STORAGE_KEY_ACCESS_TOKEN);
        if (StringUtils.isNotEmpty(accessToken)) {
            builder =
                    builder.header(
                            HeaderKeys.AUTHORIZATION,
                            String.format(HeaderValues.AUTHORIZATION_PATTERN, accessToken));
        }
        return builder;
    }

    public CheckPinResponse checkPin(String username, String password) {
        CheckPinResponse checkPinResponse =
                baseRequest(IspConstants.Endpoints.CHECK_PIN)
                        .post(
                                CheckPinResponse.class,
                                new CheckPinRequest(
                                        new CheckPinRequestPayload(username, password)));

        return checkPinResponse;
    }

    public RegisterDeviceResponse registerDevice() {
        RegisterDeviceRequestPayload payload = new RegisterDeviceRequestPayload();
        RegisterDeviceRequest request = new RegisterDeviceRequest(payload);
        RegisterDeviceResponse response =
                baseRequest(IspConstants.Endpoints.REGISTER_DEVICE)
                        .post(RegisterDeviceResponse.class, request);
        sessionStorage.put(TRANSACTION_ID_STORAGE_KEY, response.getPayload().getTransactionId());
        return response;
    }

    public RegisterDevice2Response registerDevice2(String otp) {
        RegisterDevice2RequestPayload payload =
                new RegisterDevice2RequestPayload(
                        otp, sessionStorage.get(TRANSACTION_ID_STORAGE_KEY));
        RegisterDevice2Request request = new RegisterDevice2Request(payload);
        RegisterDevice2Response response =
                baseRequest(IspConstants.Endpoints.REGISTER_DEVICE_2)
                        .post(RegisterDevice2Response.class, request);
        sessionStorage.put(TRANSACTION_ID_STORAGE_KEY, response.getPayload().getTransactionId());
        return response;
    }

    public RegisterDevice3Response registerDevice3(String deviceName, String deviceId, String pin) {
        RegisterDevice3RequestPayload payload =
                new RegisterDevice3RequestPayload(
                        deviceName, deviceId, pin, sessionStorage.get(TRANSACTION_ID_STORAGE_KEY));
        RegisterDevice3Request request = new RegisterDevice3Request(payload);
        RegisterDevice3Response response =
                baseRequest(IspConstants.Endpoints.REGISTER_DEVICE_3)
                        .post(RegisterDevice3Response.class, request);
        sessionStorage.put(TRANSACTION_ID_STORAGE_KEY, response.getPayload().getTransactionId());
        return response;
    }

    public CheckTimeResponse checkTime(String username, String deviceId, Instant deviceTime) {
        CheckTimeRequest request = new CheckTimeRequest(username, deviceId, deviceTime);
        return baseRequest(IspConstants.Endpoints.CHECK_TIME)
                .post(CheckTimeResponse.class, request);
    }

    public ConfirmDeviceResponse confirmDevice(String deviceId, String totp) {
        ConfirmDeviceRequestPayload payload =
                new ConfirmDeviceRequestPayload(
                        deviceId, totp, sessionStorage.get(TRANSACTION_ID_STORAGE_KEY));
        ConfirmDeviceRequest request = new ConfirmDeviceRequest(payload);
        return baseRequest(IspConstants.Endpoints.CONFIRM_DEVICE)
                .post(ConfirmDeviceResponse.class, request);
    }

    public BaseResponse disableAllBookmark(String deviceId) {
        return baseRequest(IspConstants.Endpoints.DISABLE_ALL_BOOKMARKS)
                .header(HeaderKeys.DEVICE_ID, deviceId)
                .post(BaseResponse.class, NULL_PAYLOAD_JSON);
    }
}
