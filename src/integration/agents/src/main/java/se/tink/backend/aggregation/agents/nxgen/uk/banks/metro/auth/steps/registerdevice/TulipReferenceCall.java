package se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.steps.registerdevice;

import agents_platform_agents_framework.org.springframework.http.MediaType;
import agents_platform_agents_framework.org.springframework.http.RequestEntity;
import agents_platform_agents_framework.org.springframework.http.ResponseEntity;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.MetroServiceConstants.Services;
import se.tink.backend.aggregation.agentsplatform.agentsframework.common.AgentExtendedClientInfo;
import se.tink.backend.aggregation.agentsplatform.agentsframework.http.AgentHttpClient;
import se.tink.backend.aggregation.agentsplatform.agentsframework.http.AgentSimpleExternalApiCall;
import se.tink.backend.aggregation.agentsplatform.agentsframework.http.ExternalApiCallResult;

public class TulipReferenceCall extends AgentSimpleExternalApiCall<String, String, String, String> {

    private String tulipReference;

    public TulipReferenceCall(AgentHttpClient httpClient) {
        super(httpClient, String.class);
    }

    @Override
    protected RequestEntity<String> prepareRequest(
            String tulipReference, AgentExtendedClientInfo clientInfo) {
        this.tulipReference = tulipReference;
        return RequestEntity.post(Services.TULIP_SERVICE.url().path("mobile/conf").build())
                .headers(Services.TULIP_SERVICE.defaultHeaders())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(
                        String.format(
                                "session_id=%s&osVersion=%s&org_id=%s&os=%s&sdk_version=%s",
                                tulipReference, "12.4.5", "30wp1pjj", "iOS", "6.0-92"));
    }

    @Override
    protected ExternalApiCallResult<String> parseResponse(ResponseEntity<String> httpResponse) {
        return new ExternalApiCallResult<>(tulipReference);
    }
}
