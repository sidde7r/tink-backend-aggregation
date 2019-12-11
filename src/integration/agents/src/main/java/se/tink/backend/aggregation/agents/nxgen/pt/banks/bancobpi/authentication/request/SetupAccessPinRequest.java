package se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.authentication.request;

import se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.common.DefaultRequest;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.common.RequestException;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.entity.BancoBpiAuthContext;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;

public class SetupAccessPinRequest extends DefaultRequest<SetupAccessPinResponse> {

    private static final String URL =
            "https://apps.bancobpi.pt/BPIAPP/screenservices/BPIAPP/Fiabilizacao/PIN/ActionMobileInitiateRegistarDispositivoFidelizado";
    private static final String BODY_TEMPLATE =
            "{\"versionInfo\": {\"moduleVersion\": \"%s\",\"apiVersion\": \"kLvBIVZMfpTpRxE83Y5Hnw\"},\"viewName\": \"Fiabilizacao.PIN\",\"inputParameters\": {\"Pin\": \"%s\",\"IdDispositivo\": \"%s\",\"MobileChallengeResponse\": {\"Id\": \"\",\"Response\": \"\"}}}";

    private final String pin;

    public SetupAccessPinRequest(final BancoBpiAuthContext authContext) {
        super(authContext, URL);
        this.pin = authContext.getAccessPin();
    }

    @Override
    protected RequestBuilder withSpecificHeaders(
            TinkHttpClient httpClient, RequestBuilder requestBuilder) {
        return requestBuilder;
    }

    @Override
    public RequestBuilder withBody(TinkHttpClient httpClient, RequestBuilder requestBuilder) {
        return requestBuilder.body(
                String.format(BODY_TEMPLATE, getModuleVersion(), pin, getDeviceUUID()));
    }

    @Override
    public SetupAccessPinResponse execute(
            RequestBuilder requestBuilder, final TinkHttpClient httpClient)
            throws RequestException {
        return new SetupAccessPinResponse(requestBuilder.post(String.class));
    }
}
