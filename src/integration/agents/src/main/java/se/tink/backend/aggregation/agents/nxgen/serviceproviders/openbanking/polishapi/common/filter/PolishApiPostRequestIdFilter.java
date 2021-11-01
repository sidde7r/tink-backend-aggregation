package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.common.filter;

import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.configuration.PolishApiConstants;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.filter.engine.FilterOrder;
import se.tink.backend.aggregation.nxgen.http.filter.engine.FilterPhases;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.libraries.serialization.utils.SerializationUtils;

@RequiredArgsConstructor
@FilterOrder(category = FilterPhases.REQUEST_HANDLE, order = Integer.MAX_VALUE - 1)
public class PolishApiPostRequestIdFilter extends Filter {

    private final AgentComponentProvider agentComponentProvider;

    @Override
    public HttpResponse handle(HttpRequest httpRequest)
            throws HttpClientException, HttpResponseException {
        String requestId = getUuid();
        setRequestIdInHeaders(httpRequest, requestId);
        setRequestIdInBody(httpRequest, requestId);
        return nextFilter(httpRequest);
    }

    private String getUuid() {
        return agentComponentProvider.getRandomValueGenerator().generateUUIDv1().toString();
    }

    private void setRequestIdInHeaders(HttpRequest httpRequest, String requestId) {
        httpRequest
                .getHeaders()
                .putSingle(PolishApiConstants.Headers.HeaderKeys.X_REQUEST_ID, requestId);
    }

    private void setRequestIdInBody(HttpRequest httpRequest, String requestId) {
        String body = SerializationUtils.serializeToString(httpRequest.getBody());
        if (body != null) {
            body = body.replace(PolishApiConstants.Headers.X_REQUEST_ID_PLACEHOLDER, requestId);
            httpRequest.setBody(SerializationUtils.deserializeFromString(body, Object.class));
        }
    }
}
