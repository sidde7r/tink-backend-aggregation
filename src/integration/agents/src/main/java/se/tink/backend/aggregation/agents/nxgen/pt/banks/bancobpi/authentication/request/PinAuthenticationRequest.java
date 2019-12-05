package se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.authentication.request;

import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.authentication.BancoBpiUserState;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;

public class PinAuthenticationRequest extends DefaultRequest<LoginResponse> {

    private static final String BODY_TEMPLATE =
            "{\"versionInfo\":{\"moduleVersion\":\"fkXs_sGDv6trPlpganAgKA\",\"apiVersion\":\"Px0uCRvJc6Tj7RQjvILrUg\"},\"viewName\":\"Common.Login\",\"inputParameters\":{\"Pin\":\"%s\",\"IdDispositivo\":\"%s\",\"Device\":{\"CordovaVersion\":\"4.5.5\",\"Model\":\"iPhone9,3\",\"Platform\":\"iOS\",\"UUID\":\"%s\",\"Version\":\"12.4\",\"Manufacturer\":\"Apple\",\"IsVirtual\":false,\"Serial\":\"unknown\"}}}";
    private static final String URL =
            "https://apps.bancobpi.pt/BPIAPP/screenservices/CSM_BPIApp/ActionLogin";
    private final String accessPin;
    private final String deviceUUID;

    public PinAuthenticationRequest(BancoBpiUserState userState) {
        super(userState, URL);
        accessPin = userState.getAccessPin();
        deviceUUID = userState.getDeviceUUID();
    }

    @Override
    protected RequestBuilder withSpecificHeaders(
            TinkHttpClient httpClient, RequestBuilder requestBuilder) {
        return requestBuilder;
    }

    @Override
    public RequestBuilder withBody(TinkHttpClient httpClient, RequestBuilder requestBuilder) {
        return requestBuilder.body(String.format(BODY_TEMPLATE, accessPin, deviceUUID, deviceUUID));
    }

    @Override
    public LoginResponse execute(RequestBuilder requestBuilder, TinkHttpClient httpClient)
            throws LoginException {
        return new LoginResponse(requestBuilder.post(String.class), httpClient);
    }
}
