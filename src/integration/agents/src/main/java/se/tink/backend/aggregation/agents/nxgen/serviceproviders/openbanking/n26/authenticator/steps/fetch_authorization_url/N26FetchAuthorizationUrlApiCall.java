package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.authenticator.steps.fetch_authorization_url;

import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.N26Constants.QueryValues;

import agents_platform_agents_framework.org.springframework.http.HttpMethod;
import agents_platform_agents_framework.org.springframework.http.RequestEntity;
import agents_platform_agents_framework.org.springframework.http.ResponseEntity;
import java.net.URI;
import lombok.SneakyThrows;
import org.apache.http.client.utils.URIBuilder;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.N26Constants.QueryKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.N26Constants.Url;
import se.tink.backend.aggregation.agentsplatform.agentsframework.common.AgentExtendedClientInfo;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.InvalidRequestError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.ServerError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.http.AgentHttpClient;
import se.tink.backend.aggregation.agentsplatform.agentsframework.http.AgentSimpleExternalApiCall;
import se.tink.backend.aggregation.agentsplatform.agentsframework.http.ExternalApiCallResult;

public class N26FetchAuthorizationUrlApiCall
        extends AgentSimpleExternalApiCall<
                N26FetchAuthorizationUrlApiCallParameters, URI, String, String> {

    private final N26FetchAuthorizationUrlApiParameters apiParameters;

    public N26FetchAuthorizationUrlApiCall(
            AgentHttpClient httpClient, N26FetchAuthorizationUrlApiParameters apiParameters) {
        super(httpClient, String.class);
        this.apiParameters = apiParameters;
    }

    @SneakyThrows
    @Override
    protected RequestEntity<String> prepareRequest(
            N26FetchAuthorizationUrlApiCallParameters parameters,
            AgentExtendedClientInfo clientInfo) {
        URI callUri =
                new URIBuilder(apiParameters.getBaseUrl() + Url.AUTHORIZE)
                        .addParameter(QueryKeys.CLIENT_ID, parameters.getClientId())
                        .addParameter(QueryKeys.SCOPE, apiParameters.getScope())
                        .addParameter(QueryKeys.CODE_CHALLENGE, parameters.getCodeChallenge())
                        .addParameter(QueryKeys.REDIRECT_URL, parameters.getRedirectUri())
                        .addParameter(QueryKeys.STATE, parameters.getState())
                        .addParameter(QueryKeys.RESPONSE_TYPE, QueryValues.CODE)
                        .build();
        return new RequestEntity<>(null, HttpMethod.GET, callUri);
    }

    @Override
    protected ExternalApiCallResult<URI> parseResponse(ResponseEntity<String> httpResponse) {
        if (httpResponse.getStatusCode().is3xxRedirection()) {
            URI redirectUri = httpResponse.getHeaders().getLocation();
            return new ExternalApiCallResult<>(redirectUri);
        } else if (httpResponse.getStatusCode().is5xxServerError()) {
            return new ExternalApiCallResult<>(new ServerError());
        }
        return new ExternalApiCallResult<>(new InvalidRequestError());
    }
}
