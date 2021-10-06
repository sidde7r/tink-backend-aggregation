package se.tink.backend.aggregation.agents.nxgen.be.banks.argenta.error;

import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.nxgen.be.banks.argenta.ArgentaConstants.LogTags;
import se.tink.backend.aggregation.agents.nxgen.be.banks.argenta.authenticator.rpc.ArgentaErrorResponse;
import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;

@Slf4j
public class ArgentaKnownErrorsFilter extends Filter {

    @Override
    public HttpResponse handle(HttpRequest httpRequest) {
        HttpResponse response = nextFilter(httpRequest);
        if (response.getStatus() >= 400 && response.hasBody()) {
            ArgentaErrorResponse argentaErrorResponse;
            try {
                argentaErrorResponse = response.getBody(ArgentaErrorResponse.class);
            } catch (HttpClientException e) {
                log.warn(
                        "{} Couldn't map response body into ArgentaErrorResponse.class",
                        LogTags.ARGENTA_LOG_TAG,
                        e);
                return response;
            }
            ArgentaKnownErrorResponsesHandler.handleKnownErrorResponses(argentaErrorResponse);
        }
        return response;
    }
}
