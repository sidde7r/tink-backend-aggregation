package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.authenticator.steps.validate_consent;

import agents_platform_agents_framework.org.springframework.http.RequestEntity;
import agents_platform_agents_framework.org.springframework.http.ResponseEntity;
import java.net.URI;
import javax.ws.rs.core.HttpHeaders;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.N26Constants.Url;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.ConsentDetailsResponse;
import se.tink.backend.aggregation.agentsplatform.agentsframework.common.AgentExtendedClientInfo;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.InvalidRequestError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.ServerError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.http.AgentHttpClient;
import se.tink.backend.aggregation.agentsplatform.agentsframework.http.AgentSimpleExternalApiCall;
import se.tink.backend.aggregation.agentsplatform.agentsframework.http.ExternalApiCallResult;

public class N26ValidateConsentApiCall
        extends AgentSimpleExternalApiCall<
                N26ValidateConsentParameters,
                ConsentDetailsResponse,
                Void,
                ConsentDetailsResponse> {

    private final String baseUrl;

    public N26ValidateConsentApiCall(AgentHttpClient httpClient, String baseUrl) {
        super(httpClient, ConsentDetailsResponse.class);
        this.baseUrl = baseUrl;
    }

    @Override
    protected RequestEntity<Void> prepareRequest(
            N26ValidateConsentParameters arg, AgentExtendedClientInfo clientInfo) {
        URI callUri = prepareConsentURI(arg.getConsentId());

        return RequestEntity.get(callUri)
                .header(HttpHeaders.AUTHORIZATION, String.format("Bearer %s", arg.getAccessToken()))
                .build();
    }

    @Override
    protected ExternalApiCallResult<ConsentDetailsResponse> parseResponse(
            ResponseEntity<ConsentDetailsResponse> httpResponse) {
        if (httpResponse.getStatusCode().is2xxSuccessful()) {
            return new ExternalApiCallResult(httpResponse.getBody());
        } else if (httpResponse.getStatusCode().is5xxServerError()) {
            return new ExternalApiCallResult<>(new ServerError());
        }
        return new ExternalApiCallResult<>(new InvalidRequestError());
    }

    private URI prepareConsentURI(String consentId) {
        String inputString = baseUrl + Url.CONSENT_DETAILS;
        return URI.create(inputString.replace("{consentId}", consentId));
    }
}
