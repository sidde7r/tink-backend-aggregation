package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.authenticator.steps.fetch_token;

import agents_platform_agents_framework.com.google.common.base.Splitter;
import agents_platform_agents_framework.org.springframework.http.HttpMethod;
import agents_platform_agents_framework.org.springframework.http.RequestEntity;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.N26Constants.BodyParam;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.N26Constants.Url;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.authenticator.steps.N26ProcessStateAccessor;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.redirect.RedirectFetchTokenCallAuthenticationParameters;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.redirect.oauth2.OAuth2RedirectFetchTokenCall;
import se.tink.backend.aggregation.agentsplatform.agentsframework.common.AgentExtendedClientInfo;
import se.tink.backend.aggregation.agentsplatform.agentsframework.http.AgentHttpClient;
import se.tink.backend.aggregation.agentsplatform.framework.encode.UrlEncoder;

public class N26OAuth2RedirectFetchTokenCall extends OAuth2RedirectFetchTokenCall {

    private final N26FetchTokenParameters n26FetchTokenParameters;
    private final ObjectMapper objectMapper;

    public N26OAuth2RedirectFetchTokenCall(
            AgentHttpClient httpClient,
            N26FetchTokenParameters n26FetchTokenParameters,
            ObjectMapper objectMapper) {
        super(httpClient);
        this.n26FetchTokenParameters = n26FetchTokenParameters;
        this.objectMapper = objectMapper;
    }

    @Override
    protected Map<String, String> getClientSpecificHeaders(
            RedirectFetchTokenCallAuthenticationParameters parameters) {
        return Collections.emptyMap();
    }

    @Override
    protected RequestEntity<String> prepareRequest(
            RedirectFetchTokenCallAuthenticationParameters parameters,
            AgentExtendedClientInfo clientInfo) {
        RequestEntity<String> requestEntity = super.prepareRequest(parameters, clientInfo);

        Map<String, String> map =
                new HashMap<>(
                        Splitter.on('&')
                                .trimResults()
                                .withKeyValueSeparator('=')
                                .split(Objects.requireNonNull(requestEntity.getBody())));
        map.remove("client_id");
        String fixedBodyString = UrlEncoder.encodeToUrl(map);

        return new RequestEntity(
                fixedBodyString,
                requestEntity.getHeaders(),
                HttpMethod.POST,
                this.getAccessTokenEndpoint());
    }

    @Override
    protected Map<String, String> getClientSpecificParams(
            RedirectFetchTokenCallAuthenticationParameters parameters) {
        Map<String, String> clientSpecificParamsMap = new HashMap<>();

        clientSpecificParamsMap.put(
                BodyParam.CODE_VERIFIER,
                new N26ProcessStateAccessor(
                                parameters.getAuthenticationProcessState(), objectMapper)
                        .getN26ProcessStateData()
                        .getCodeVerifier());
        return clientSpecificParamsMap;
    }

    @Override
    protected URI getRedirectUrl() {
        return URI.create(n26FetchTokenParameters.getRedirectUrl());
    }

    @Override
    protected String getClientId(RedirectFetchTokenCallAuthenticationParameters parameters) {
        return n26FetchTokenParameters.getClientId();
    }

    @Override
    protected URI getAccessTokenEndpoint() {
        return URI.create(
                n26FetchTokenParameters.getBaseUrl()
                        + Url.TOKEN
                        + n26FetchTokenParameters.getScope());
    }
}
