package se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.authentication.request;

import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.common.DefaultRequest;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.entity.BancoBpiUserState;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;

public class SetupAccessPinRequest extends DefaultRequest<SetupAccessPinResponse> {

    private static final String URL =
            "https://apps.bancobpi.pt/BPIAPP/screenservices/BPIAPP/Fiabilizacao/PIN/ActionMobileInitiateRegistarDispositivoFidelizado";
    private static final String BODY_TEMPLATE =
            "{\"versionInfo\": {\"moduleVersion\": \"gS+lXxFxC_wWYvNlPJM_Qw\",\"apiVersion\": \"kLvBIVZMfpTpRxE83Y5Hnw\"},\"viewName\": \"Fiabilizacao.PIN\",\"inputParameters\": {\"Pin\": \"%s\",\"IdDispositivo\": \"%s\",\"MobileChallengeResponse\": {\"Id\": \"\",\"Response\": \"\"}}}";

    private final String pin;

    public SetupAccessPinRequest(final BancoBpiUserState userState) {
        super(userState, URL);
        this.pin = userState.getAccessPin();
    }

    @Override
    protected RequestBuilder withSpecificHeaders(
            TinkHttpClient httpClient, RequestBuilder requestBuilder) {
        return requestBuilder;
    }

    @Override
    public RequestBuilder withBody(TinkHttpClient httpClient, RequestBuilder requestBuilder) {
        return requestBuilder.body(String.format(BODY_TEMPLATE, pin, getDeviceUUID()));
    }

    @Override
    public SetupAccessPinResponse execute(
            RequestBuilder requestBuilder, final TinkHttpClient httpClient) throws LoginException {
        return new SetupAccessPinResponse(requestBuilder.post(String.class));
    }
}
