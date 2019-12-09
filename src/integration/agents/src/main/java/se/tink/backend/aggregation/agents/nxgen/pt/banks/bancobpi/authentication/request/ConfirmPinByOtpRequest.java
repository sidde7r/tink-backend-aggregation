package se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.authentication.request;

import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.authentication.BancoBpiUserState;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.authentication.MobileChallengeRequestedToken;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;

public class ConfirmPinByOtpRequest extends DefaultRequest<AuthenticationResponse> {

    private static final String URL =
            "https://apps.bancobpi.pt/BPIAPP/screenservices/BPIAPP/Fiabilizacao/Code/ActionMobileExecuteRegistarDispositivoFidelizado";
    private static final String BODY_TEMPLATE =
            "{\"versionInfo\": {\"moduleVersion\": \"gS+lXxFxC_wWYvNlPJM_Qw\",\"apiVersion\": \"jR8qd1rTdzHYUcSU5Wk3nA\"},\"viewName\": \"Fiabilizacao.Code\",\"inputParameters\": {\"IdDispositivo\": \"%s\",\"Pin\": \"%s\",\"MobileChallengeResponse\": {\"Id\": \"%s\",\"Response\": \"{\\\"id\\\":\\\"%s\\\",\\\"data\\\":[{\\\"requestedOTP\\\":\\\"%s\\\",\\\"mobilePhoneNumber\\\":\\\"%s\\\",\\\"processedOn\\\":\\\"%s\\\",\\\"processedOnSpecified\\\":true,\\\"uuid\\\":\\\"%s\\\",\\\"replywith\\\":\\\"%s\\\"}]}\"}}}";
    private final String pin;
    private final MobileChallengeRequestedToken mobileChallengeRequestedToken;
    private final String otpCode;

    public ConfirmPinByOtpRequest(final BancoBpiUserState userState, final String otpCode) {
        super(userState, URL);
        this.otpCode = otpCode;
        this.pin = userState.getAccessPin();
        this.mobileChallengeRequestedToken = userState.getMobileChallengeRequestedToken();
    }

    @Override
    public RequestBuilder withBody(TinkHttpClient httpClient, RequestBuilder requestBuilder) {
        return requestBuilder.body(
                String.format(
                        BODY_TEMPLATE,
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
            RequestBuilder requestBuilder, final TinkHttpClient httpClient) throws LoginException {
        return new AuthenticationResponse(requestBuilder.post(String.class));
    }

    @Override
    protected RequestBuilder withSpecificHeaders(
            TinkHttpClient httpClient, RequestBuilder requestBuilder) {
        return requestBuilder;
    }
}
