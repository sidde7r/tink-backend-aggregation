package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec;

import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.authenticator.BecAuthenticationException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.authenticator.rpc.LoginErrorResponse;
import se.tink.backend.aggregation.nxgen.http.DefaultResponseStatusHandler;
import se.tink.backend.aggregation.nxgen.http.handler.HttpResponseStatusHandler;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.libraries.serialization.utils.SerializationUtils;

@RequiredArgsConstructor
public class BecResponseStatusHandler implements HttpResponseStatusHandler {

    private final DefaultResponseStatusHandler defaultResponseStatusHandler;

    BecResponseStatusHandler() {
        this(new DefaultResponseStatusHandler());
    }

    @Override
    public void handleResponse(HttpRequest httpRequest, HttpResponse httpResponse) {
        if (httpResponse.getStatus() == 400) {

            LoginErrorResponse baem =
                    SerializationUtils.deserializeFromString(
                            httpResponse.getBody(String.class), LoginErrorResponse.class);

            String message = baem == null ? "Unknown error occurred." : baem.getMessage();

            throw new BecAuthenticationException(message);
        } else {
            defaultResponseStatusHandler.handleResponse(httpRequest, httpResponse);
        }
    }

    @Override
    public void handleResponseWithoutExpectedReturnBody(
            HttpRequest httpRequest, HttpResponse httpResponse) {
        defaultResponseStatusHandler.handleResponseWithoutExpectedReturnBody(
                httpRequest, httpResponse);
    }
}
