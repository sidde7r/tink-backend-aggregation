package se.tink.backend.aggregation.agents.agentplatform;

import agents_platform_agents_framework.org.springframework.http.HttpStatus;
import agents_platform_agents_framework.org.springframework.http.RequestEntity;
import agents_platform_agents_framework.org.springframework.http.ResponseEntity;
import agents_platform_agents_framework.org.springframework.util.LinkedMultiValueMap;
import agents_platform_agents_framework.org.springframework.util.MultiValueMap;
import lombok.var;
import se.tink.backend.aggregation.agentsplatform.agentsframework.common.AgentExtendedClientInfo;
import se.tink.backend.aggregation.agentsplatform.agentsframework.http.AgentHttpClient;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.request.HttpMethod;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;

/**
 * The purpose of this class of being a proxy for the TinkHttpClient, is to switch the
 * implementation for the Spring RestTemplate in the future, when agents will be migrating to the
 * Agent Platform
 */
public class AgentPlatformHttpClient implements AgentHttpClient {

    private final TinkHttpClient tinkHttpClient;

    public AgentPlatformHttpClient(TinkHttpClient tinkHttpClient) {
        this.tinkHttpClient = tinkHttpClient;
    }

    public <T> ResponseEntity<T> exchange(RequestEntity<?> requestEntity, Class<T> responseType) {
        RequestBuilder requestBuilder =
                tinkHttpClient
                        .request(requestEntity.getUrl().toString())
                        .body(requestEntity.getBody());

        requestEntity
                .getHeaders()
                .forEach(
                        (headerName, headerValues) ->
                                headerValues.forEach(
                                        headerValue ->
                                                requestBuilder.header(headerName, headerValue)));
        var response =
                requestBuilder.method(
                        HttpMethod.valueOf(requestEntity.getMethod().name()), HttpResponse.class);
        return new ResponseEntity<>(
                response.getBody(responseType),
                getResponseHeaders(response),
                HttpStatus.valueOf(response.getStatus()));
    }

    private MultiValueMap<String, String> getResponseHeaders(HttpResponse response) {
        LinkedMultiValueMap newHeaders = new LinkedMultiValueMap<String, String>();
        response.getHeaders().forEach(newHeaders::addAll);
        return newHeaders;
    }

    @Override
    public <T> ResponseEntity<T> exchange(
            RequestEntity<?> requestEntity,
            Class<T> responseType,
            AgentExtendedClientInfo extendedClientInfo) {
        return exchange(requestEntity, responseType);
    }
}
