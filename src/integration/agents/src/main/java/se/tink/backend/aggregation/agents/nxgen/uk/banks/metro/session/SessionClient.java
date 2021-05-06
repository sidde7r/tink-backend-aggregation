package se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.session;

import agents_platform_agents_framework.org.springframework.http.RequestEntity;
import agents_platform_agents_framework.org.springframework.http.ResponseEntity;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.agentplatform.AgentPlatformHttpClient;
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
                        .build();
        ResponseEntity<String> exchange = httpClient.exchange(requestEntity, String.class);
        return exchange.getStatusCode().isError();
    }
}
