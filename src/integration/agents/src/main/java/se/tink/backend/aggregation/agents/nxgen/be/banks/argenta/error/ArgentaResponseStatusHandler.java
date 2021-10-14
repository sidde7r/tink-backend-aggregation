package se.tink.backend.aggregation.agents.nxgen.be.banks.argenta.error;

import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.nxgen.be.banks.argenta.ArgentaConstants.ErrorResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.argenta.ArgentaConstants.LogTags;
import se.tink.backend.aggregation.agents.nxgen.be.banks.argenta.authenticator.rpc.ArgentaErrorResponse;
import se.tink.backend.aggregation.nxgen.http.DefaultResponseStatusHandler;
import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;

@Slf4j
public class ArgentaResponseStatusHandler extends DefaultResponseStatusHandler {

    @Override
    public void handleResponse(HttpRequest httpRequest, HttpResponse httpResponse) {

        if (httpResponse.getStatus() == 400 && httpResponse.hasBody()) {
            try {
                ArgentaErrorResponse argentaErrorResponse =
                        httpResponse.getBody(ArgentaErrorResponse.class);
                if (ErrorResponse.ERROR_SIGNING_STEPUP_REQUIRED.equalsIgnoreCase(
                        argentaErrorResponse.getCode())) {
                    return;
                }
            } catch (HttpClientException e) {
                log.warn(
                        "{} Couldn't map response body into {}",
                        LogTags.ARGENTA_LOG_TAG,
                        ArgentaErrorResponse.class.getSimpleName(),
                        e);
                super.handleResponse(httpRequest, httpResponse);
            }
        }
        super.handleResponse(httpRequest, httpResponse);
    }
}
