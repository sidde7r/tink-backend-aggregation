package se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.authentication.request;

import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.authentication.BancoBpiUserState;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;

public abstract class DefaultRequest<RESPONSE> implements Request<RESPONSE> {

    private static final String HEADER_CSRF_TOKEN = "X-CSRFToken";
    private static final String HEADER_DEVICE_UUID = "OutSystems-device-uuid";
    private final String csrfToken;
    private final String deviceUUID;
    private final String url;

    DefaultRequest(final String csrfToken, final String deviceUUID, final String url) {
        this.csrfToken = csrfToken;
        this.deviceUUID = deviceUUID;
        this.url = url;
    }

    DefaultRequest(final BancoBpiUserState userState, final String url) {
        this(userState.getSessionCSRFToken(), userState.getDeviceUUID(), url);
    }

    @Override
    public RequestBuilder withHeaders(
            final TinkHttpClient httpClient, final RequestBuilder requestBuilder)
            throws LoginException {
        return withSpecificHeaders(
                httpClient,
                requestBuilder
                        .header(HEADER_CSRF_TOKEN, csrfToken)
                        .header(HEADER_DEVICE_UUID, deviceUUID));
    }

    @Override
    public RequestBuilder withUrl(final TinkHttpClient httpClient) {
        return httpClient.request(url);
    }

    protected abstract RequestBuilder withSpecificHeaders(
            final TinkHttpClient httpClient, final RequestBuilder requestBuilder);

    public String getCsrfToken() {
        return csrfToken;
    }

    public String getDeviceUUID() {
        return deviceUUID;
    }

    public String getUrl() {
        return url;
    }
}
