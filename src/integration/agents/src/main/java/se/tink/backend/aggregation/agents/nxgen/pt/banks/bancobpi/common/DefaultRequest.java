package se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.common;

import se.tink.backend.aggregation.agents.common.Request;
import se.tink.backend.aggregation.agents.common.RequestException;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.entity.BancoBpiAuthContext;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;

public abstract class DefaultRequest<RESPONSE> implements Request<RESPONSE> {

    private static final String HEADER_CSRF_TOKEN = "X-CSRFToken";
    private static final String HEADER_DEVICE_UUID = "OutSystems-device-uuid";
    private final String csrfToken;
    private final String deviceUUID;
    private final String url;
    private final String moduleVersion;

    protected DefaultRequest(BancoBpiAuthContext authContext, final String url) {
        this.csrfToken = authContext.getSessionCSRFToken();
        this.deviceUUID = authContext.getDeviceUUID();
        this.moduleVersion = authContext.getModuleVersion();
        this.url = url;
    }

    @Override
    public RequestBuilder withHeaders(
            final TinkHttpClient httpClient, final RequestBuilder requestBuilder)
            throws RequestException {
        return withSpecificHeaders(
                httpClient,
                requestBuilder
                        .header(HEADER_CSRF_TOKEN, getCsrfToken())
                        .header(HEADER_DEVICE_UUID, getDeviceUUID()));
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

    protected String getModuleVersion() {
        return moduleVersion;
    }
}
