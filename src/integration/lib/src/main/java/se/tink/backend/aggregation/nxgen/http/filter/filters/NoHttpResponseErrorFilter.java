package se.tink.backend.aggregation.nxgen.http.filter.filters;

import com.google.common.base.Strings;
import com.sun.jersey.api.client.ClientHandlerException;
import java.util.Objects;
import se.tink.backend.aggregation.agents.exceptions.errors.BankServiceError;
import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.exceptions.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.filter.Filter;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;

/**
 * Utility filter to throw a Tink {@link BankServiceError} when an API call get no HTTP response.
 * </code>.
 */
public class NoHttpResponseErrorFilter extends Filter {

    private static final String NO_RESPONSE_MSG = "failed to respond";

    @Override
    public HttpResponse handle(HttpRequest httpRequest)
            throws HttpClientException, HttpResponseException {
        try {
            return nextFilter(httpRequest);
        } catch (ClientHandlerException e) {
            final Throwable exceptionCause = e.getCause();
            if (Objects.isNull(exceptionCause)
                    || Strings.isNullOrEmpty(exceptionCause.getMessage())
                    || !exceptionCause.getMessage().toLowerCase().contains(NO_RESPONSE_MSG)) {
                throw e;
            }

            throw BankServiceError.BANK_SIDE_FAILURE.exception(NO_RESPONSE_MSG);
        }
    }
}
