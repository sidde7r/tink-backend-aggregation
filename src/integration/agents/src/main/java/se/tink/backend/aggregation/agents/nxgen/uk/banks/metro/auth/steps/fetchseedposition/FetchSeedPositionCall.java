package se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.steps.fetchseedposition;

import static io.vavr.API.Match;
import static se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.MetroServiceConstants.HEADER_VERSION;

import agents_platform_agents_framework.org.springframework.http.RequestEntity;
import agents_platform_agents_framework.org.springframework.http.ResponseEntity;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.vavr.control.Option;
import java.util.UUID;
import lombok.SneakyThrows;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.GlobalConstants;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.MetroServiceConstants;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.MetroServiceConstants.Services;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.model.rpc.mobileapp.SecurityNumberSeedResponse;
import se.tink.backend.aggregation.agentsplatform.agentsframework.common.AgentExtendedClientInfo;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.AgentBankApiError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.http.AgentHttpClient;
import se.tink.backend.aggregation.agentsplatform.agentsframework.http.AgentSimpleExternalApiCall;
import se.tink.backend.aggregation.agentsplatform.agentsframework.http.ExternalApiCallResult;

public class FetchSeedPositionCall
        extends AgentSimpleExternalApiCall<
                FetchSeedPositionParameters, SecurityNumberSeedResponse, Void, String> {

    private final ObjectMapper objectMapper;

    public FetchSeedPositionCall(AgentHttpClient httpClient, ObjectMapper objectMapper) {
        super(httpClient, String.class);
        this.objectMapper = objectMapper;
    }

    @Override
    protected RequestEntity<Void> prepareRequest(
            FetchSeedPositionParameters arg, AgentExtendedClientInfo clientInfo) {
        return RequestEntity.get(
                        MetroServiceConstants.Services.MOBILE_APP_SERVICE
                                .url()
                                .path("registration/users/" + arg.getUserId())
                                .build())
                .headers(
                        httpHeaders ->
                                httpHeaders.add(
                                        "X-REQUEST-ID",
                                        String.format(
                                                "%s-%s-%s",
                                                UUID.randomUUID().toString().toUpperCase(),
                                                GlobalConstants.PLATFORM.getValue(),
                                                HEADER_VERSION)))
                .headers(Services.MOBILE_APP_SERVICE.defaultHeaders())
                .build();
    }

    @SneakyThrows
    @Override
    protected ExternalApiCallResult<SecurityNumberSeedResponse> parseResponse(
            ResponseEntity<String> httpResponse) {
        Option<AgentBankApiError> option = Match(httpResponse).option(KnownErrors.getCases());

        return !option.isEmpty()
                ? new ExternalApiCallResult<>(option.get())
                : new ExternalApiCallResult<>(
                        objectMapper.readValue(
                                httpResponse.getBody(), SecurityNumberSeedResponse.class));
    }
}
