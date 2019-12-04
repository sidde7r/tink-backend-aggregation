package se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.authentication.request;

import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;

public class LoginRequest extends DefaultRequest<LoginResponse> {

    private static final String URL =
            "https://apps.bancobpi.pt/BPIAPP/screenservices/CSM_BPIApp/ActionLoginFiabilizacao";
    private static final String BODY_TEMPLATE =
            "{\"versionInfo\": {\"moduleVersion\": \"gS+lXxFxC_wWYvNlPJM_Qw\",\"apiVersion\": \"tOLgSMN3vyoVv0vbNpOw0w\"},\"viewName\": \"Fiabilizacao.Registration\",\"inputParameters\": {\"Username\": \"%s\",\"Password\": \"%s\",\"Device\": {\"CordovaVersion\": \"4.5.5\",\"Model\": \"iPhone9,3\",\"Platform\": \"iOS\",\"UUID\": \"%s\",\"Version\": \"12.4\",\"Manufacturer\": \"Apple\",\"IsVirtual\": false,\"Serial\": \"unknown\"}}}";
    private static final String DUMMY_CSRF_TOKEN = "zjQO2fTPctemz1nvFHTxua+cyb4=";

    private final String username;
    private final String password;

    public LoginRequest(String deviceUUID, String username, String password) {
        super(DUMMY_CSRF_TOKEN, deviceUUID, URL);
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
                String.format(BODY_TEMPLATE, username, password, getDeviceUUID()));
    }

    @Override
    public LoginResponse execute(
            final RequestBuilder requestBuilder, final TinkHttpClient httpClient)
            throws LoginException {
        return new LoginResponse(requestBuilder.post(String.class), httpClient);
    }
}
