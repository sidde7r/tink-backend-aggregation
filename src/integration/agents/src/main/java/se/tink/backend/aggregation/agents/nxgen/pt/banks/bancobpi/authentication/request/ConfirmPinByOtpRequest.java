package se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.authentication.request;

import se.tink.backend.aggregation.agents.common.RequestException;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.common.DefaultRequest;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.entity.BancoBpiEntityManager;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.entity.MobileChallengeRequestedToken;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;

public class ConfirmPinByOtpRequest extends DefaultRequest<AuthenticationResponse> {

    private static final String URL =
            "https://apps.bancobpi.pt/BPIAPP/screenservices/BPIAPP/Fiabilizacao/Code/ActionMobileExecuteRegistarDispositivoFidelizado";
    private static final String BODY_TEMPLATE =
            "{\"versionInfo\": {\"moduleVersion\": \"%s\",\"apiVersion\": \"jR8qd1rTdzHYUcSU5Wk3nA\"},\"viewName\": \"Fiabilizacao.Code\",\"inputParameters\": {\"IdDispositivo\": \"%s\",\"Pin\": \"%s\",\"MobileChallengeResponse\": {\"Id\": \"%s\",\"Response\": \"{\\\"id\\\":\\\"%s\\\",\\\"data\\\":[{\\\"requestedOTP\\\":\\\"%s\\\",\\\"mobilePhoneNumber\\\":\\\"%s\\\",\\\"processedOn\\\":\\\"%s\\\",\\\"processedOnSpecified\\\":true,\\\"uuid\\\":\\\"%s\\\",\\\"replywith\\\":\\\"%s\\\"}]}\"}}}";
    private final String pin;
    private final MobileChallengeRequestedToken mobileChallengeRequestedToken;
    private final String otpCode;

    public ConfirmPinByOtpRequest(final BancoBpiEntityManager entityManager, final String otpCode) {
        super(entityManager.getAuthContext(), URL);
        this.otpCode = otpCode;
        this.pin = entityManager.getAuthContext().getAccessPin();
        this.mobileChallengeRequestedToken =
                entityManager.getAuthContext().getMobileChallengeRequestedToken();
    }

    @Override
    public RequestBuilder withBody(TinkHttpClient httpClient, RequestBuilder requestBuilder) {
        return requestBuilder.body(
                String.format(
                        BODY_TEMPLATE,
                        getModuleVersion(),
                        getDeviceUUID(),
                        pin,
                        mobileChallengeRequestedToken.getId(),
                        mobileChallengeRequestedToken.getId(),
                        otpCode,
                        mobileChallengeRequestedToken.getPhoneNumber(),
                        mobileChallengeRequestedToken.getProcessedOn(),
                        mobileChallengeRequestedToken.getUuid(),
                        mobileChallengeRequestedToken.getReplyWith()));
    }

    @Override
    public AuthenticationResponse execute(
            RequestBuilder requestBuilder, final TinkHttpClient httpClient)
            throws RequestException {
        return new AuthenticationResponse(requestBuilder.post(String.class));
    }

    @Override
    protected RequestBuilder withSpecificHeaders(
            TinkHttpClient httpClient, RequestBuilder requestBuilder) {
        return requestBuilder;
    }
}
