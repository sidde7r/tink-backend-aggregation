package se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.steps.registerdevice;

import static se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.MetroServiceConstants.APPLICATION_ID_QUERY_PARAM;
import static se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.MetroServiceConstants.EN_US;
import static se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.MetroServiceConstants.LOCALE_QUERY_PARAM;
import static se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.MetroServiceConstants.MOBILE_METRO_APPLICATION;

import agents_platform_agents_framework.org.springframework.http.MediaType;
import agents_platform_agents_framework.org.springframework.http.RequestEntity;
import agents_platform_agents_framework.org.springframework.http.ResponseEntity;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.MetroServiceConstants.Services;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.model.rpc.tlc.device.DeviceOperationRequest;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.model.rpc.tlc.device.RegisterDeviceOperationResponse;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.fetcher.common.error.UnknownError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.common.AgentExtendedClientInfo;
import se.tink.backend.aggregation.agentsplatform.agentsframework.http.AgentHttpClient;
import se.tink.backend.aggregation.agentsplatform.agentsframework.http.AgentSimpleExternalApiCall;
import se.tink.backend.aggregation.agentsplatform.agentsframework.http.ExternalApiCallResult;
import se.tink.libraries.serialization.utils.SerializationUtils;

@Slf4j
public class RegisterDeviceCall
        extends AgentSimpleExternalApiCall<
                DeviceOperationRequest,
                RegisterDeviceOperationResponse,
                DeviceOperationRequest,
                RegisterDeviceOperationResponse> {

    public RegisterDeviceCall(AgentHttpClient httpClient) {
        super(httpClient, RegisterDeviceOperationResponse.class);
    }

    @Override
    protected RequestEntity<DeviceOperationRequest> prepareRequest(
            DeviceOperationRequest arg, AgentExtendedClientInfo clientInfo) {
        return RequestEntity.post(
                        Services.AUTHENTICATION_SERVICE
                                .url()
                                .path("bind")
                                .queryParam(APPLICATION_ID_QUERY_PARAM, MOBILE_METRO_APPLICATION)
                                .queryParam(LOCALE_QUERY_PARAM, EN_US)
                                .build())
                .contentType(MediaType.APPLICATION_JSON)
                .headers(Services.AUTHENTICATION_SERVICE.defaultHeaders())
                .body(arg);
    }

    @Override
    protected ExternalApiCallResult<RegisterDeviceOperationResponse> parseResponse(
            ResponseEntity<RegisterDeviceOperationResponse> httpResponse) {
        if (httpResponse.getStatusCode().isError()) {
            return new ExternalApiCallResult<>(
                    new UnknownError(
                            httpResponse.getStatusCode(),
                            SerializationUtils.serializeToString(httpResponse.getBody())));
        }

        return new ExternalApiCallResult<>(httpResponse.getBody());
    }
}
