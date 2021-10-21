package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fintecsystems.filters;

import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fintecsystems.FinTecSystemsConstants.ErrorMessages.ERROR_MESSAGE_401;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fintecsystems.FinTecSystemsConstants.ErrorMessages.ERROR_MESSAGE_403;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fintecsystems.FinTecSystemsConstants.ErrorMessages.ERROR_MESSAGE_500;

import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fintecsystems.error.FTSErrorResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fintecsystems.error.FTSException;
import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

@Slf4j
public class FTSExceptionFilter extends Filter {

    @Override
    public HttpResponse handle(HttpRequest httpRequest)
            throws HttpClientException, HttpResponseException {
        HttpResponse httpResponse = nextFilter(httpRequest);
        log.error(
                "Error from FTS code:{} and body:{}",
                httpResponse.getStatus(),
                httpResponse.getBody(String.class));

        switch (httpResponse.getStatus()) {
            case 401:
                throw new FTSException(ERROR_MESSAGE_401);
            case 403:
                throw new FTSException(ERROR_MESSAGE_403);
            case 500:
                throw new FTSException(ERROR_MESSAGE_500);
            case 422:
                handleInputErrors(httpResponse);
        }
        return httpResponse;
    }

    private void handleInputErrors(HttpResponse httpResponse) {
        FTSErrorResponse body = null;
        try {
            body = httpResponse.getBody(FTSErrorResponse.class);
            log.error("Error from FTS code:422 and body:{}", body);
            throw new FTSException(body.getMessage());
        } catch (RuntimeException e) {
            // Could not parse as ErrorResponse.class or some other error during it, skip trying.
            log.error("error while parsing FTS error", e);
        }
    }
}
