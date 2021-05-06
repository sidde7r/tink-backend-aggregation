package se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.steps.autoauthentication;

import static se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.MetroServiceConstants.APPLICATION_ID_QUERY_PARAM;
import static se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.MetroServiceConstants.CONTENT_SIGNATURE;
import static se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.MetroServiceConstants.MOBILE_METRO_APPLICATION;

import agents_platform_agents_framework.org.springframework.http.MediaType;
import agents_platform_agents_framework.org.springframework.http.RequestEntity;
import agents_platform_agents_framework.org.springframework.http.ResponseEntity;
import java.net.URI;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.MetroServiceConstants.Services;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.model.error.UnknownError;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.model.rpc.tlc.device.AuthorizationOperationResponse;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.model.rpc.tlc.device.DeviceOperationRequest;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.utils.ContentSigner;
import se.tink.backend.aggregation.agentsplatform.agentsframework.common.AgentExtendedClientInfo;
import se.tink.backend.aggregation.agentsplatform.agentsframework.http.AgentHttpClient;
import se.tink.backend.aggregation.agentsplatform.agentsframework.http.AgentSimpleExternalApiCall;
import se.tink.backend.aggregation.agentsplatform.agentsframework.http.ExternalApiCallResult;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class AutoAuthenticationCall
        extends AgentSimpleExternalApiCall<
                AutoAuthenticationParameters,
                AuthorizationOperationResponse,
                DeviceOperationRequest,
                AuthorizationOperationResponse> {

    public AutoAuthenticationCall(AgentHttpClient httpClient) {
        super(httpClient, AuthorizationOperationResponse.class);
    }

    @Override
    protected RequestEntity<DeviceOperationRequest> prepareRequest(
            AutoAuthenticationParameters arg, AgentExtendedClientInfo clientInfo) {
        URI url =
                Services.AUTHENTICATION_SERVICE
                        .url()
                        .path("login")
                        .queryParam(APPLICATION_ID_QUERY_PARAM, MOBILE_METRO_APPLICATION)
                        .queryParam("did", arg.getDeviceId())
                        .build();
        return RequestEntity.post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .headers(Services.AUTHENTICATION_SERVICE.defaultHeaders())
                .header(
                        CONTENT_SIGNATURE,
                        ContentSigner.signHeaderContentSignature(
                                arg.getRequest(),
                                arg.getSignaturePrivateKey(),
                                url,
                                arg.getDeviceId()))
                .body(arg.getRequest());
    }

    @Override
    protected ExternalApiCallResult<AuthorizationOperationResponse> parseResponse(
            ResponseEntity<AuthorizationOperationResponse> httpResponse) {
        if (httpResponse.getStatusCode().isError()) {
            return new ExternalApiCallResult<>(
                    new UnknownError(
                            httpResponse.getStatusCode(),
                            SerializationUtils.serializeToString(httpResponse.getBody())));
        }
        return new ExternalApiCallResult<>(httpResponse.getBody());
    }
}
