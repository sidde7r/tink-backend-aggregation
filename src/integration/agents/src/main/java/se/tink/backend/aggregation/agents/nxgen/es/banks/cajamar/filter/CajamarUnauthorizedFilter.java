package se.tink.backend.aggregation.agents.nxgen.es.banks.cajamar.filter;

import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.es.banks.cajamar.CajamarConstants.ErrorCodes;
import se.tink.backend.aggregation.agents.nxgen.es.banks.cajamar.rpc.CajamarErrorResponse;
import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

public class CajamarUnauthorizedFilter extends Filter {

    @Override
    public HttpResponse handle(HttpRequest httpRequest)
            throws HttpClientException, HttpResponseException {
        HttpResponse response = nextFilter(httpRequest);
        CajamarErrorResponse errorResponse = response.getBody(CajamarErrorResponse.class);

        if (ErrorCodes.APP_PROBLEM.equals(errorResponse.getCode())) {
            throw SessionError.SESSION_EXPIRED.exception();
        }
        if (response.getStatus() == 401) {
            throw LoginError.INCORRECT_CREDENTIALS.exception();
        }

        return response;
    }
}
