package se.tink.backend.aggregation.agents.nxgen.it.banks.ing.authenticator.registration;

import static se.tink.backend.aggregation.nxgen.http.request.HttpMethod.POST;

import lombok.Builder;
import lombok.Value;
import se.tink.backend.aggregation.agents.nxgen.it.banks.ing.ConfigurationProvider;
import se.tink.backend.aggregation.agents.nxgen.it.banks.ing.authenticator.registration.TokenActivationSuccessExternalApiCall.Arg;
import se.tink.backend.aggregation.agents.nxgen.it.banks.ing.authenticator.registration.TokenActivationSuccessExternalApiCall.Result;
import se.tink.backend.aggregation.agents.nxgen.it.banks.ing.scaffold.ExternalApiCallResult;
import se.tink.backend.aggregation.agents.nxgen.it.banks.ing.scaffold.SimpleExternalApiCall;
import se.tink.backend.aggregation.nxgen.http.HttpRequestImpl;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.form.Form;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public class TokenActivationSuccessExternalApiCall extends SimpleExternalApiCall<Arg, Result> {

    private final ConfigurationProvider configurationProvider;

    TokenActivationSuccessExternalApiCall(
            TinkHttpClient httpClient, ConfigurationProvider configurationProvider) {
        super(httpClient);
        this.configurationProvider = configurationProvider;
    }

    @Override
    protected HttpRequest prepareRequest(Arg arg) {
        return new HttpRequestImpl(
                POST,
                new URL(
                        String.format(
                                "%s%s",
                                configurationProvider.getBaseUrl(),
                                "/MobileFlow/tokenActivationSuccess.htm")),
                prepareBody(arg));
    }

    private String prepareBody(Arg arg) {
        return Form.builder()
                .encodeSpacesWithPercent()
                .put(
                        "succesLabel",
                        "Complimenti, il Token è stato correttamente attivato su questo dispositivo.")
                .put("aid", arg.getActivationId())
                .put("oid", arg.getActivationId())
                .put("otml_context", "c1")
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
    }

    @Builder
    @Value
    static class Result {}
}
