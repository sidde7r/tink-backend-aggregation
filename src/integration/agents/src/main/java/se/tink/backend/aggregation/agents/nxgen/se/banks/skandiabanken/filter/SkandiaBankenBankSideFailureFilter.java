package se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.filter;

import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.rpc.ErrorResponse;
import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

public class SkandiaBankenBankSideFailureFilter extends Filter {
    /**
     * Skandiabanken can sometimes raise "Exception of type
     * 'Helium.Api.Common.Exceptions.HeliumApiException' was thrown." even though we are receiving a
     * 200 status code response. This filter will throw that as a BANK_SIDE_FAILURE.
     *
     * @param httpRequest
     * @return
     * @throws HttpClientException
     * @throws HttpResponseException
     */
    @Override
    public HttpResponse handle(HttpRequest httpRequest)
            throws HttpClientException, HttpResponseException {
        HttpResponse response = nextFilter(httpRequest);

        if (isBankSideFailure(response)) {
            throw BankServiceError.BANK_SIDE_FAILURE.exception(
                    response.getBody(ErrorResponse.class).getErrorMessage());
        }
        return response;
    }

    public static boolean isBankSideFailure(HttpResponse response) {
        try {
            ErrorResponse errorResponse = response.getBody(ErrorResponse.class);
            if (errorResponse != null) {
                // For some requests, we are receiving an empty body which is why we need this check
                // to avoid NPE:s.
                return errorResponse.isBankRaisingApiException();
            }
        } catch (HttpClientException e) {
            // If the response can't be parsed into an ErrorResponse, then we are not receiving such
            // an error and a HttpClientException will be raised.
            return false;
        }
        return false;
    }
}
