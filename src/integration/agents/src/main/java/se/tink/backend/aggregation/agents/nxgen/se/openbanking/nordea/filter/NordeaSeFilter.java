package se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordea.filter;

import org.apache.http.HttpStatus;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.rpc.NordeaErrorResponse;
import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

public class NordeaSeFilter extends Filter {

    private static final int TOO_MANY_REQUEST_HTTP_STATUS = 429;

    @Override
    public HttpResponse handle(HttpRequest httpRequest)
            throws HttpClientException, HttpResponseException {

        HttpResponse response = nextFilter(httpRequest);

        int status = response.getStatus();
        if (status == HttpStatus.SC_INTERNAL_SERVER_ERROR || status == HttpStatus.SC_UNAUTHORIZED) {
            NordeaErrorResponse errorResponse = response.getBody(NordeaErrorResponse.class);

            if (errorResponse.isBankSideFailure() || errorResponse.isFetchCertificateFailure()) {
                throw BankServiceError.BANK_SIDE_FAILURE.exception();
            }
        }

        if (status == TOO_MANY_REQUEST_HTTP_STATUS) {
            throw BankServiceError.NO_BANK_SERVICE.exception();
        }

        return response;
    }
}
