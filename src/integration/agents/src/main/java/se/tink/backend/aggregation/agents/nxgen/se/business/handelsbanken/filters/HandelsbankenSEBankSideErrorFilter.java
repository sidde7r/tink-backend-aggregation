package se.tink.backend.aggregation.agents.nxgen.se.business.handelsbanken.filters;

import io.vavr.control.Try;
import javax.ws.rs.core.MediaType;
import org.apache.http.HttpStatus;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.nxgen.se.business.handelsbanken.rpc.ErrorResponse;
import se.tink.backend.aggregation.agents.nxgen.se.business.handelsbanken.rpc.XmlErrorResponse;
import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

public class HandelsbankenSEBankSideErrorFilter extends Filter {

    @Override
    public HttpResponse handle(HttpRequest httpRequest)
            throws HttpClientException, HttpResponseException {
        HttpResponse httpResponse = nextFilter(httpRequest);
        if (httpResponse.getStatus() == HttpStatus.SC_INTERNAL_SERVER_ERROR
                && MediaType.APPLICATION_JSON_TYPE.isCompatible(httpResponse.getType())) {

            ErrorResponse response = httpResponse.getBody(ErrorResponse.class);
            if (httpResponse.hasBody() && response.isTmpBankSideFailure()) {
                throw BankServiceError.BANK_SIDE_FAILURE.exception(
                        String.format(
                                "Error code: %s, message: %s",
                                response.getStatus(), response.getDetail()));
            }
        } else if (MediaType.APPLICATION_XML_TYPE.isCompatible(httpResponse.getType())) {
            // No check for response status as they return http status 200

            // if failed to parse the response to XmlErrorResponse don't throw exception
            XmlErrorResponse xmlError = httpResponse.getBody(XmlErrorResponse.class);
            if (Try.of(xmlError::isServiceUnavailable).getOrElse(false)) {
                throw BankServiceError.NO_BANK_SERVICE.exception(
                        String.format(
                                "Error code: %s, message: %s",
                                xmlError.getCode(), xmlError.getLabel()));
            }
        }
        return httpResponse;
    }
}
