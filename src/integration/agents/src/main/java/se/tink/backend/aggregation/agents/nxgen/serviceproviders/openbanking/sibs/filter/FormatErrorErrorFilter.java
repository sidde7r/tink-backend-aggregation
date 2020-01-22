package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.filter;

import org.apache.commons.lang3.StringUtils;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.filter.engine.FilterOrder;
import se.tink.backend.aggregation.nxgen.http.filter.engine.FilterPhases;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

/**
 * Utility filter to throw a Tink {@link BankServiceError} when an SIBS API call responds with
 * <code>
 * HTTP 400 Bad Request</code>.
 *
 * <p>but body must contain FORMAT_ERROR. This is a temporary workaround to convert FORMAT_ERROR
 * into CONSENT_EXPIRED as the reason of error is the same according to SIBS provided clarification.
 *
 * <p>Typical response body:
 *
 * <p>{"transactionStatus":"RJCT","tppMessages":[{"category":"ERROR","code":"FORMAT_ERROR","text":"Format
 * of certain request fields are not matching the XS2A requirements. An explicit path to the
 * corresponding field might be added in the return message."}]}
 */
@FilterOrder(category = FilterPhases.REQUEST_HANDLE, order = Integer.MIN_VALUE)
public final class FormatErrorErrorFilter extends Filter {

    private static final int FORMAT_ERROR = 400;
    private static final String FORMAT_ERROR_MESSAGE = "FORMAT_ERROR";

    @Override
    public HttpResponse handle(final HttpRequest httpRequest)
            throws HttpClientException, HttpResponseException {
        final HttpResponse response = nextFilter(httpRequest);

        if (response.getStatus() == FORMAT_ERROR) {
            String body = response.getBody(String.class);
            if (StringUtils.containsIgnoreCase(body, FORMAT_ERROR_MESSAGE)) {
                throw BankServiceError.CONSENT_INVALID.exception(
                        "Http status: " + response.getStatus() + " Error body: " + body);
            }
        }

        return response;
    }
}
