package se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.session;

import static se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.MetroServiceConstants.HEADER_VERSION;

import agents_platform_agents_framework.org.springframework.http.RequestEntity;
import agents_platform_agents_framework.org.springframework.http.ResponseEntity;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.agentplatform.AgentPlatformHttpClient;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.GlobalConstants;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.MetroServiceConstants.Services;

@RequiredArgsConstructor
class SessionClient {
    private final AgentPlatformHttpClient httpClient;

    void logout() {
        RequestEntity<Void> requestEntity =
                RequestEntity.get(
                                Services.MOBILE_APP_SERVICE
                                        .url()
                                        .path("customer/logout/device")
                                        .build())
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
                        .build();
        httpClient.exchange(requestEntity, String.class);
    }

    boolean isSessionExpired() {
        RequestEntity<Void> requestEntity =
                RequestEntity.get(
                                Services.MOBILE_APP_SERVICE
                                        .url()
                                        .path("customer/contact-details")
                                        .build())
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
                        .build();
        ResponseEntity<String> exchange = httpClient.exchange(requestEntity, String.class);
        return exchange.getStatusCode().isError();
    }
}
