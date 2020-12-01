package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.filters;

import org.apache.http.HttpStatus;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.rpc.NordeaErrorResponse;
import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

public class BankSideFailureFilter extends Filter {
    @Override
    public HttpResponse handle(HttpRequest httpRequest)
            throws HttpClientException, HttpResponseException {
        HttpResponse response = nextFilter(httpRequest);

        if (isBankSideFailure(response)) {
            throw BankServiceError.BANK_SIDE_FAILURE.exception();
        }
        return response;
    }

    public static boolean isBankSideFailure(HttpResponse response) {
        int status = response.getStatus();
        if ((status == HttpStatus.SC_INTERNAL_SERVER_ERROR || status == HttpStatus.SC_UNAUTHORIZED)
                && response.hasBody()) {
            NordeaErrorResponse nordeaErrorResponse = getBodyAsExpectedType(response);
            return nordeaErrorResponse != null
                    && (nordeaErrorResponse.isBankSideFailure()
                            || nordeaErrorResponse.isFetchCertificateFailure());
        }
        return false;
    }

    private static NordeaErrorResponse getBodyAsExpectedType(HttpResponse response) {
        try {
            return response.getBody(NordeaErrorResponse.class);
        } catch (RuntimeException e) {
            return null;
        }
    }
}
