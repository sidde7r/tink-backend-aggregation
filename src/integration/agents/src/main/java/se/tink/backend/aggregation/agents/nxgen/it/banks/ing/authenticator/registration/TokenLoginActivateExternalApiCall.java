package se.tink.backend.aggregation.agents.nxgen.it.banks.ing.authenticator.registration;

import static se.tink.backend.aggregation.nxgen.http.request.HttpMethod.POST;

import java.time.Clock;
import lombok.Builder;
import lombok.Value;
import se.tink.backend.aggregation.agents.nxgen.it.banks.ing.ConfigurationProvider;
import se.tink.backend.aggregation.agents.nxgen.it.banks.ing.authenticator.registration.TokenLoginActivateExternalApiCall.Arg;
import se.tink.backend.aggregation.agents.nxgen.it.banks.ing.authenticator.registration.TokenLoginActivateExternalApiCall.Result;
import se.tink.backend.aggregation.nxgen.http.HttpRequestImpl;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.form.Form;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.scaffold.ExternalApiCallResult;
import se.tink.backend.aggregation.nxgen.scaffold.SimpleExternalApiCall;

class TokenLoginActivateExternalApiCall extends SimpleExternalApiCall<Arg, Result> {

    private final ConfigurationProvider configurationProvider;
    private final Clock clock;

    TokenLoginActivateExternalApiCall(
            TinkHttpClient httpClient, ConfigurationProvider configurationProvider, Clock clock) {
        super(httpClient);
        this.configurationProvider = configurationProvider;
        this.clock = clock;
    }

    @Override
    protected HttpRequest prepareRequest(Arg arg) {
        return new HttpRequestImpl(
                POST,
                new URL(
                        String.format(
                                "%s%s",
                                configurationProvider.getBaseUrl(),
                                "/MobileFlow/tokenLoginActivate.htm")),
                prepareBody(arg));
    }

    private String prepareBody(Arg arg) {
        return Form.builder()
                .put("oid", arg.getActivationId())
                .put("otml_context", "c1")
                .put("challengeId", arg.getChallengeId())
                .put("mt_action_sign_otp", arg.getMtActionSignOtp())
                .put(
                        "mt_action_sign_datiop",
                        String.format("%s|1|%s", arg.getMtActionSignDataiopPart(), clock.millis()))
                .put("aid", arg.getActivationId())
                .build()
                .serialize();
    }

    @Override
    protected ExternalApiCallResult<Result> parseResponse(HttpResponse httpResponse) {
        return ExternalApiCallResult.of(Result.builder().build(), httpResponse.getStatus());
    }

    @Builder
    @Value
    static class Arg {

        private String activationId;
        private String challengeId;
        private String mtActionSignOtp;
        private String mtActionSignDataiopPart;
    }

    @Builder
    @Value
    static class Result {}
}
