package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.authenticator.steps.fetch_consent;

import static se.tink.backend.aggregation.agents.utils.berlingroup.BerlingroupConstants.FormValues;

import agents_platform_agents_framework.org.springframework.http.MediaType;
import agents_platform_agents_framework.org.springframework.http.RequestEntity;
import agents_platform_agents_framework.org.springframework.http.ResponseEntity;
import java.net.URI;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import javax.ws.rs.core.HttpHeaders;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.N26Constants.Url;
import se.tink.backend.aggregation.agents.utils.berlingroup.BerlingroupConstants.AccessEntityTypes;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.AccessEntity;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.ConsentRequest;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.ConsentResponse;
import se.tink.backend.aggregation.agentsplatform.agentsframework.common.AgentExtendedClientInfo;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.InvalidRequestError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.ServerError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.http.AgentHttpClient;
import se.tink.backend.aggregation.agentsplatform.agentsframework.http.AgentSimpleExternalApiCall;
import se.tink.backend.aggregation.agentsplatform.agentsframework.http.ExternalApiCallResult;

public class N26FetchConsentApiCall
        extends AgentSimpleExternalApiCall<
                N26FetchConsentParameters, ConsentResponse, ConsentRequest, ConsentResponse> {

    private final String baseUrl;

    public N26FetchConsentApiCall(AgentHttpClient httpClient, String baseUrl) {
        super(httpClient, ConsentResponse.class);
        this.baseUrl = baseUrl;
    }

    @Override
    protected RequestEntity<ConsentRequest> prepareRequest(
            N26FetchConsentParameters arg, AgentExtendedClientInfo clientInfo) {

        AccessEntity accessEntity = new AccessEntity(AccessEntityTypes.ALL_ACCOUNTS_OWNER_NAME);
        ConsentRequest consentRequest =
                new ConsentRequest(
                        accessEntity,
                        FormValues.TRUE,
                        LocalDate.now().plusDays(89).format(DateTimeFormatter.ISO_DATE),
                        FormValues.FREQUENCY_PER_DAY,
                        FormValues.FALSE);

        return RequestEntity.post(URI.create(baseUrl + Url.CONSENT_FETCH))
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, String.format("Bearer %s", arg.getAccessToken()))
                .body(consentRequest);
    }

    @Override
    protected ExternalApiCallResult<ConsentResponse> parseResponse(
            ResponseEntity<ConsentResponse> httpResponse) {
        if (httpResponse.getStatusCode().is2xxSuccessful()) {
            return new ExternalApiCallResult(httpResponse.getBody());
        } else if (httpResponse.getStatusCode().is5xxServerError()) {
            return new ExternalApiCallResult<>(new ServerError());
        }
        return new ExternalApiCallResult<>(new InvalidRequestError());
    }
}
