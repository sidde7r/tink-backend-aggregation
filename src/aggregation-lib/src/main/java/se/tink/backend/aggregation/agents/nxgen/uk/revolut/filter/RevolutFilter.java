package se.tink.backend.aggregation.agents.nxgen.uk.revolut.filter;

import se.tink.backend.aggregation.agents.nxgen.uk.revolut.RevolutConstants;
import se.tink.backend.aggregation.nxgen.http.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.filter.Filter;

public class RevolutFilter extends Filter {

    @Override
    public HttpResponse handle(HttpRequest httpRequest) throws HttpClientException, HttpResponseException {
        if (httpRequest.getHeaders().getFirst(RevolutConstants.AppAuthenticationValues.API_VERSION.getKey()) == null) {
            httpRequest.getHeaders().add(RevolutConstants.AppAuthenticationValues.API_VERSION.getKey(),
                    RevolutConstants.AppAuthenticationValues.API_VERSION.getValue());
        }

        if (httpRequest.getHeaders().getFirst(RevolutConstants.AppAuthenticationValues.APP_VERSION.getKey()) == null) {
            httpRequest.getHeaders().add(RevolutConstants.AppAuthenticationValues.APP_VERSION.getKey(),
                    RevolutConstants.AppAuthenticationValues.APP_VERSION.getValue());
        }

        if (httpRequest.getHeaders().getFirst(RevolutConstants.AppAuthenticationValues.MODEL.getKey()) == null) {
            httpRequest.getHeaders().add(RevolutConstants.AppAuthenticationValues.MODEL.getKey(),
                    RevolutConstants.AppAuthenticationValues.MODEL.getValue());
        }

        if (httpRequest.getHeaders().getFirst(RevolutConstants.AppAuthenticationValues.USER_AGENT.getKey()) == null) {
            httpRequest.getHeaders().add(RevolutConstants.AppAuthenticationValues.USER_AGENT.getKey(),
                    RevolutConstants.AppAuthenticationValues.USER_AGENT.getValue());
        }

        return nextFilter(httpRequest);
    }
}
