package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.filters;

import io.vavr.control.Try;
import javax.ws.rs.core.MediaType;
import org.apache.http.HttpStatus;
import se.tink.backend.aggregation.agents.exceptions.errors.BankServiceError;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.rpc.ErrorResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.rpc.XmlErrorResponse;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.filter.Filter;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;

public class HandelsbankenSEBankSideErrorFilter extends Filter {

    @Override
    public HttpResponse handle(HttpRequest httpRequest)
            throws HttpClientException, HttpResponseException {
        HttpResponse httpResponse = nextFilter(httpRequest);
        if (httpResponse.getStatus() == HttpStatus.SC_INTERNAL_SERVER_ERROR
                && MediaType.APPLICATION_JSON_TYPE.isCompatible(httpResponse.getType())) {

            if (httpResponse.hasBody()
                    && httpResponse.getBody(ErrorResponse.class).isTmpBankSideFailure()) {
                throw BankServiceError.BANK_SIDE_FAILURE.exception();
            }
        } else if (MediaType.APPLICATION_XML_TYPE.isCompatible(httpResponse.getType())) {
            // No check for response status as they return http status 200

            // if failed to parse the response to XmlErrorResponse don't throw exception
            if (Try.of(() -> httpResponse.getBody(XmlErrorResponse.class).isServiceUnavailable())
                    .getOrElse(false)) {
                throw BankServiceError.NO_BANK_SERVICE.exception();
            }
        }
        return httpResponse;
    }
}
