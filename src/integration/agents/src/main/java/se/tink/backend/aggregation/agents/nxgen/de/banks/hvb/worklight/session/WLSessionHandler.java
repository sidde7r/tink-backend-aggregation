package se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.worklight.session;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.worklight.WLApiClient;
import se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.worklight.WLConfig;
import se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.worklight.WLConstants;
import se.tink.backend.aggregation.nxgen.http.Form;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;

public final class WLSessionHandler {
    private final WLApiClient client;
    private final WLSessionHandlerStorage storage;
    private final WLConfig config;

    public WLSessionHandler(final WLApiClient client, final WLSessionHandlerStorage storage, final WLConfig config) {
        this.client = client;
        this.storage = storage;
        this.config = config;
    }

    /**
     * @return E.g. "https://example.com/MyModule/apps/services/api/MyModule/iphone/<slug>"
     */
    private String getApiUrl(final String slug) {
        return config.getEndpointUrl() + WLConstants.Url.API_ROOT + config.getModuleName() + slug;
    }

    public HttpResponse logout() {
        final Form form = Form.builder()
                .put(WLConstants.Forms.REALM, WLConstants.Forms.REALM_VALUE)
                .build();
        final HttpResponse response = client.getClient().request(getApiUrl(WLConstants.Url.LOGOUT))
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_TYPE)
                .header(WLConstants.Headers.X_WL_APP_VERSION, WLConstants.WL_APP_VERSION)
                .header(WLConstants.Storage.WL_INSTANCE_ID, storage.getOptionalWlInstanceId().orElse(""))
                .body(form.serialize())
                .post(HttpResponse.class);
        return response;
    }

    public HttpResponse heartbeat() throws SessionException {
        final String wlInstanceId = storage.getOptionalWlInstanceId()
                .orElseThrow(SessionError.SESSION_EXPIRED::exception);
        return client.getClient().request(getApiUrl(WLConstants.Url.HEARTBEAT))
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_TYPE)
                .header(WLConstants.Headers.X_WL_APP_VERSION, WLConstants.WL_APP_VERSION)
                .header(WLConstants.Storage.WL_INSTANCE_ID, wlInstanceId)
                .post(HttpResponse.class);
    }
}
