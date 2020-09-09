package se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.filter;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import org.apache.http.HttpStatus;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.IcaBankenConstants;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.rpc.ErrorResponse;
import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

public class IcaBankenFilter extends Filter {

    @Override
    public HttpResponse handle(HttpRequest httpRequest)
            throws HttpClientException, HttpResponseException {
        MultivaluedMap<String, Object> headers = httpRequest.getHeaders();

        headers.add(IcaBankenConstants.Headers.ACCEPT, MediaType.APPLICATION_JSON);
        headers.add(
                IcaBankenConstants.Headers.HEADER_API_VERSION,
                IcaBankenConstants.Headers.VALUE_API_VERSION);
        headers.add(
                IcaBankenConstants.Headers.HEADER_APIKEY, IcaBankenConstants.Headers.VALUE_APIKEY);
        headers.add(
                IcaBankenConstants.Headers.HEADER_CLIENTAPPVERSION,
                IcaBankenConstants.Headers.VALUE_CLIENTAPPVERSION);
        headers.add(
                IcaBankenConstants.Headers.HEADER_CLIENT_OS,
                IcaBankenConstants.Headers.VALUE_CLIENT_OS);
        headers.add(
                IcaBankenConstants.Headers.HEADER_CLIENT_OS_VERSION,
                IcaBankenConstants.Headers.VALUE_CLIENT_OS_VERSION);
        headers.add(
                IcaBankenConstants.Headers.HEADER_CLIENT_HARDWARE,
                IcaBankenConstants.Headers.VALUE_CLIENT_HARDWARE);

        HttpResponse response = nextFilter(httpRequest);

        if (response.getStatus() == HttpStatus.SC_INTERNAL_SERVER_ERROR) {
            ErrorResponse errorResponse = response.getBody(ErrorResponse.class);

            if (errorResponse.isBanksideFailureError()) {
                throw BankServiceError.BANK_SIDE_FAILURE.exception(
                        String.format(
                                "Http status: %d, error message: %s",
                                HttpStatus.SC_INTERNAL_SERVER_ERROR,
                                errorResponse.getResponseStatus().getClientMessage()));
            }
        }

        return response;
    }
}
