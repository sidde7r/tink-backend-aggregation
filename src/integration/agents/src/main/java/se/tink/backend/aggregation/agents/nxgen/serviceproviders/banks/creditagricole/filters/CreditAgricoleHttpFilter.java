package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.creditagricole.filters;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.creditagricole.CreditAgricoleConstants.ConstantHeader;
import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

public class CreditAgricoleHttpFilter extends Filter {

    private void addHeaderIfNotPresent(HttpRequest httpRequest, ConstantHeader constantHeader) {
        if (!httpRequest.getHeaders().containsKey(constantHeader.getKey())) {
            httpRequest.getHeaders().add(constantHeader.getKey(), constantHeader.getValue());
        }
    }

    @Override
    public HttpResponse handle(HttpRequest httpRequest)
            throws HttpClientException, HttpResponseException {
        addHeaderIfNotPresent(httpRequest, ConstantHeader.USER_AGENT);
        return nextFilter(httpRequest);
    }
}
