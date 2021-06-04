package se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.filter;

import se.tink.backend.aggregation.agents.exceptions.errors.SupplementalInfoError;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.EvoBancoConstants.ErrorCodes;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.filter.entity.EvoBancoErrorResponse;
import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

public class EvoBancoTokenInvalidFilter extends Filter {

    @Override
    public HttpResponse handle(HttpRequest httpRequest)
            throws HttpClientException, HttpResponseException {
        HttpResponse response = nextFilter(httpRequest);
        EvoBancoErrorResponse errorResponse = response.getBody(EvoBancoErrorResponse.class);

        if (ErrorCodes.INVALID_TOKEN.equals(errorResponse.getResponse().getCode())) {
            throw SupplementalInfoError.NO_VALID_CODE.exception();
        }

        return response;
    }
}
