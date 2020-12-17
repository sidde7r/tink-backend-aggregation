package se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.steps.challange;

import static io.vavr.API.Match;
import static se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.MetroServiceConstants.APPLICATION_ID_QUERY_PARAM;
import static se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.MetroServiceConstants.CONTENT_SIGNATURE;
import static se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.MetroServiceConstants.EN_US;
import static se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.MetroServiceConstants.LOCALE_QUERY_PARAM;
import static se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.MetroServiceConstants.MOBILE_METRO_APPLICATION;

import agents_platform_agents_framework.org.springframework.http.MediaType;
import agents_platform_agents_framework.org.springframework.http.RequestEntity;
import agents_platform_agents_framework.org.springframework.http.ResponseEntity;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.vavr.control.Option;
import java.net.URI;
import lombok.SneakyThrows;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.MetroServiceConstants.Services;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.model.rpc.tlc.asserts.ConfirmChallengeRequest;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.model.rpc.tlc.asserts.ConfirmChallengeResponse;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.utils.ContentSigner;
import se.tink.backend.aggregation.agentsplatform.agentsframework.common.AgentExtendedClientInfo;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.AgentBankApiError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.http.AgentHttpClient;
import se.tink.backend.aggregation.agentsplatform.agentsframework.http.AgentSimpleExternalApiCall;
import se.tink.backend.aggregation.agentsplatform.agentsframework.http.ExternalApiCallResult;

public class ConfirmChallengeCall
        extends AgentSimpleExternalApiCall<
                ConfirmChallengeParameters,
                ConfirmChallengeResponse,
                ConfirmChallengeRequest,
                String> {

    private final ObjectMapper objectMapper;

    public ConfirmChallengeCall(AgentHttpClient httpClient, ObjectMapper objectMapper) {
        super(httpClient, String.class);
        this.objectMapper = objectMapper;
    }

    @Override
    protected RequestEntity<ConfirmChallengeRequest> prepareRequest(
            ConfirmChallengeParameters arg, AgentExtendedClientInfo clientInfo) {
        URI url =
                Services.AUTHENTICATION_SERVICE
                        .url()
                        .path("assert")
                        .queryParam(APPLICATION_ID_QUERY_PARAM, MOBILE_METRO_APPLICATION)
                        .queryParam("did", arg.getDeviceId())
                        .queryParam(LOCALE_QUERY_PARAM, EN_US)
                        .queryParam("sid", arg.getSessionId())
                        .build();
        return RequestEntity.post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .headers(Services.AUTHENTICATION_SERVICE.defaultHeaders())
                .header(
                        CONTENT_SIGNATURE,
                        ContentSigner.signHeaderContentSignature(
                                arg.getRequest(),
                                arg.getSigningHeaderKey(),
                                url,
                                arg.getDeviceId()))
                .body(arg.getRequest());
    }

    @SneakyThrows
    @Override
    protected ExternalApiCallResult<ConfirmChallengeResponse> parseResponse(
            ResponseEntity<String> httpResponse) {
        Option<AgentBankApiError> option = Match(httpResponse).option(KnownErrors.getCases());

        return !option.isEmpty()
                ? new ExternalApiCallResult<>(option.get())
                : new ExternalApiCallResult<>(
                        objectMapper.readValue(
                                httpResponse.getBody(), ConfirmChallengeResponse.class));
    }
}
