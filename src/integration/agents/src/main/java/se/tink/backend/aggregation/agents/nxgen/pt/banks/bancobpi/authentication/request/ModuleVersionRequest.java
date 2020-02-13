package se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.authentication.request;

import org.json.JSONException;
import org.json.JSONObject;
import se.tink.backend.aggregation.agents.common.RequestException;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.common.DefaultRequest;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.entity.BancoBpiAuthContext;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;

public class ModuleVersionRequest extends DefaultRequest<String> {

    private static final String URL =
            "https://apps.bancobpi.pt/BPIAPP/moduleservices/moduleversioninfo?";
    private static final String HEADER_OS_VISITOR = "osVisitor";

    @Override
    public String getUrl() {
        return super.getUrl() + System.currentTimeMillis();
    }

    public ModuleVersionRequest(BancoBpiAuthContext authContext) {
        super(authContext, URL);
    }

    @Override
    public RequestBuilder withHeaders(RequestBuilder requestBuilder) {
        return getDeviceUUID() != null
                ? requestBuilder.cookie(HEADER_OS_VISITOR, getDeviceUUID())
                : requestBuilder;
    }

    @Override
    public RequestBuilder withBody(RequestBuilder requestBuilder) {
        return requestBuilder;
    }

    @Override
    public String execute(RequestBuilder requestBuilder) throws RequestException {
        return extractModuleVersion(requestBuilder.get(String.class));
    }

    private String extractModuleVersion(String rawJsonResponse) throws RequestException {
        try {
            JSONObject response = new JSONObject(rawJsonResponse);
            return response.getString("versionToken");
        } catch (JSONException e) {
            throw new RequestException("Unexpected response format");
        }
    }
}
