package se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.authentication.request;

import se.tink.backend.aggregation.agents.common.RequestException;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.common.DefaultRequest;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.entity.BancoBpiAuthContext;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;

public class LoginRequest extends DefaultRequest<LoginResponse> {

    private static final String URL =
            "https://apps.bancobpi.pt/BPIAPP/screenservices/CSM_BPIApp/ActionLoginFiabilizacao";
    private static final String BODY_TEMPLATE =
            "{\"versionInfo\": {\"moduleVersion\": \"%s\",\"apiVersion\": \"tOLgSMN3vyoVv0vbNpOw0w\"},\"viewName\": \"Fiabilizacao.Registration\",\"inputParameters\": {\"Username\": \"%s\",\"Password\": \"%s\",\"Device\": {\"CordovaVersion\": \"4.5.5\",\"Model\": \"iPhone9,3\",\"Platform\": \"iOS\",\"UUID\": \"%s\",\"Version\": \"12.4\",\"Manufacturer\": \"Apple\",\"IsVirtual\": false,\"Serial\": \"unknown\"}}}";
    private static final String DUMMY_CSRF_TOKEN = "zjQO2fTPctemz1nvFHTxua+cyb4=";

    private final String username;
    private final String password;

    public LoginRequest(BancoBpiAuthContext authContext, String username, String password) {
        super(authContext, URL);
        this.username = username;
        this.password = password;
    }

    @Override
    protected RequestBuilder withSpecificHeaders(
            final TinkHttpClient httpClient, final RequestBuilder requestBuilder) {
        return requestBuilder;
    }

    @Override
    public RequestBuilder withBody(
            final TinkHttpClient httpClient, final RequestBuilder requestBuilder) {
        return requestBuilder.body(
                String.format(
                        BODY_TEMPLATE, getModuleVersion(), username, password, getDeviceUUID()));
    }

    @Override
    public LoginResponse execute(
            final RequestBuilder requestBuilder, final TinkHttpClient httpClient)
            throws RequestException {
        return new LoginResponse(requestBuilder.post(String.class), httpClient);
    }

    @Override
    public String getCsrfToken() {
        return DUMMY_CSRF_TOKEN;
    }
}
