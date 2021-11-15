package se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.filter;

import se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.rpc.ErrorResponse;
import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.filter.filters.randomretry.AbstractRandomRetryFilter;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;

public class SkandiaBankenRetryFilter extends AbstractRandomRetryFilter {

    /**
     * Skandiabanken can sometimes raise "Exception of type *
     * 'Helium.Api.Common.Exceptions.HeliumApiException' was thrown." even though we are receiving a
     * * 200 status code response. This filter will make sure we're retrying on such an exception.
     *
     * @param maxNumRetries Number of additional retries to be performed.
     * @param retrySleepMilliseconds Time in milliseconds that will be spent sleeping between
     */
    public SkandiaBankenRetryFilter(int maxNumRetries, long retrySleepMilliseconds) {
        super(maxNumRetries, retrySleepMilliseconds);
    }

    @Override
    protected boolean shouldRetry(HttpResponse response) {
        return isBankRaisingApiException(response);
    }

    private boolean isBankRaisingApiException(HttpResponse response) {
        try {
            ErrorResponse errorResponse = response.getBody(ErrorResponse.class);
            return errorResponse != null && errorResponse.isBankRaisingApiException();
        } catch (HttpClientException e) {
            // If the response can't be parsed into an ErrorResponse, then we are not receiving such
            // an error and a HttpClientException will be raised.
            return false;
        }
    }
}
