package se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.steps.logindevice;

import static se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.MetroServiceConstants.HEADER_VERSION;

import agents_platform_agents_framework.org.springframework.http.MediaType;
import agents_platform_agents_framework.org.springframework.http.RequestEntity;
import agents_platform_agents_framework.org.springframework.http.ResponseEntity;
import java.util.UUID;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.GlobalConstants;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.MetroServiceConstants.Services;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.model.rpc.mobileapp.LoginDeviceRequest;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.fetcher.common.error.UnknownError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.common.AgentExtendedClientInfo;
import se.tink.backend.aggregation.agentsplatform.agentsframework.http.AgentHttpClient;
import se.tink.backend.aggregation.agentsplatform.agentsframework.http.AgentSimpleExternalApiCall;
import se.tink.backend.aggregation.agentsplatform.agentsframework.http.ExternalApiCallResult;

public class LoginDeviceCall
        extends AgentSimpleExternalApiCall<
                LoginDeviceParameters, String, LoginDeviceRequest, String> {

    public LoginDeviceCall(AgentHttpClient httpClient) {
        super(httpClient, String.class);
    }

    @Override
    protected RequestEntity<LoginDeviceRequest> prepareRequest(
            LoginDeviceParameters arg, AgentExtendedClientInfo clientInfo) {
        return RequestEntity.post(
                        Services.MOBILE_APP_SERVICE.url().path("customer/sca/login/device").build())
                .contentType(MediaType.APPLICATION_JSON)
                .headers(Services.MOBILE_APP_SERVICE.defaultHeaders())
                .headers(
                        httpHeaders ->
                                httpHeaders.add(
                                        "X-REQUEST-ID",
                                        String.format(
                                                "%s-%s-%s",
                                                UUID.randomUUID().toString().toUpperCase(),
                                                GlobalConstants.PLATFORM.getValue(),
                                                HEADER_VERSION)))
                .header("X-TOKEN", arg.getToken())
                .header("X-TS-DEVICE-ID", arg.getDeviceId())
                .header("X-USER-ID", arg.getUserId())
                .body(arg.getRequest());
    }

    @Override
    protected ExternalApiCallResult<String> parseResponse(ResponseEntity<String> httpResponse) {
        if (httpResponse.getStatusCode().isError()) {
            return new ExternalApiCallResult<>(
                    new UnknownError(httpResponse.getStatusCode(), httpResponse.getBody()));
        }

        return new ExternalApiCallResult<>(httpResponse.getBody());
    }
}
