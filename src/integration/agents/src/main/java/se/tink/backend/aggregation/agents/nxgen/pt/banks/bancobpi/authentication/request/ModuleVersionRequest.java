package se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.authentication.request;

import org.json.JSONException;
import org.json.JSONObject;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.common.DefaultRequest;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.entity.BancoBpiUserState;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.libraries.i18n.LocalizableKey;

public class ModuleVersionRequest extends DefaultRequest<String> {

    private static final String URL =
            "https://apps.bancobpi.pt/BPIAPP/moduleservices/moduleversioninfo?";
    private static final String HEADER_OS_VISITOR = "osVisitor";

    @Override
    public String getUrl() {
        return super.getUrl() + System.currentTimeMillis();
    }

    public ModuleVersionRequest(BancoBpiUserState userState) {
        super(userState, URL);
    }

    @Override
    public RequestBuilder withHeaders(TinkHttpClient httpClient, RequestBuilder requestBuilder) {
        return getDeviceUUID() != null
                ? requestBuilder.cookie(HEADER_OS_VISITOR, getDeviceUUID())
                : requestBuilder;
    }

    @Override
    protected RequestBuilder withSpecificHeaders(
            TinkHttpClient httpClient, RequestBuilder requestBuilder) {
        return requestBuilder;
    }

    @Override
    public RequestBuilder withBody(TinkHttpClient httpClient, RequestBuilder requestBuilder) {
        return requestBuilder;
    }

    @Override
    public String execute(RequestBuilder requestBuilder, TinkHttpClient httpClient)
            throws LoginException {
        return extractModuleVersion(requestBuilder.get(String.class));
    }

    private String extractModuleVersion(String rawJsonResponse) throws LoginException {
        try {
            JSONObject response = new JSONObject(rawJsonResponse);
            return response.getString("versionToken");
        } catch (JSONException e) {
            throw new LoginException(
                    LoginError.NOT_SUPPORTED, new LocalizableKey("Unexpected response format"));
        }
    }
}
