package se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.authentication.request;

import se.tink.backend.aggregation.agents.common.RequestException;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.common.DefaultRequest;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.entity.BancoBpiAccountsContext;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.entity.BancoBpiEntityManager;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;

public class PinAuthenticationRequest extends DefaultRequest<PinAuthenticationResponse> {

    private static final String BODY_TEMPLATE =
            "{\"versionInfo\":{\"moduleVersion\":\"%s\",\"apiVersion\":\"Px0uCRvJc6Tj7RQjvILrUg\"},\"viewName\":\"Common.Login\",\"inputParameters\":{\"Pin\":\"%s\",\"IdDispositivo\":\"%s\",\"Device\":{\"CordovaVersion\":\"4.5.5\",\"Model\":\"iPhone9,3\",\"Platform\":\"iOS\",\"UUID\":\"%s\",\"Version\":\"12.4\",\"Manufacturer\":\"Apple\",\"IsVirtual\":false,\"Serial\":\"unknown\"}}}";
    private static final String URL =
            "https://apps.bancobpi.pt/BPIAPP/screenservices/CSM_BPIApp/ActionLogin";
    private final String accessPin;
    private BancoBpiAccountsContext bancoBpiAccountsContext;

    public PinAuthenticationRequest(BancoBpiEntityManager entityManager) {
        super(entityManager.getAuthContext(), URL);
        accessPin = entityManager.getAuthContext().getAccessPin();
        bancoBpiAccountsContext = entityManager.getAccountsContext();
    }

    @Override
    public RequestBuilder withBody(RequestBuilder requestBuilder) {
        return requestBuilder.body(
                String.format(
                        BODY_TEMPLATE,
                        getModuleVersion(),
                        accessPin,
                        getDeviceUUID(),
                        getDeviceUUID()));
    }

    @Override
    public PinAuthenticationResponse execute(RequestBuilder requestBuilder)
            throws RequestException {
        return new PinAuthenticationResponse(
                requestBuilder.post(HttpResponse.class), bancoBpiAccountsContext);
    }
}
