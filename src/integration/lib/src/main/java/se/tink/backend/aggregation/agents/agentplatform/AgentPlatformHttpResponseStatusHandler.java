package se.tink.backend.aggregation.agents.agentplatform;

import se.tink.backend.aggregation.nxgen.http.handler.HttpResponseStatusHandler;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;

public class AgentPlatformHttpResponseStatusHandler implements HttpResponseStatusHandler {

    @Override
    public void handleResponse(HttpRequest httpRequest, HttpResponse httpResponse) {
        // We want pass all HttpResponses to the agent
    }

    @Override
    public void handleResponseWithoutExpectedReturnBody(
            HttpRequest httpRequest, HttpResponse httpResponse) {
        // We want pass all HttpResponses to the agent
    }
}
