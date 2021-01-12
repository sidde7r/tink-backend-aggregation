package se.tink.backend.aggregation.events;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class IntegrationParameters {

    private String providerName;
    private String appId;
    private String clusterId;
    private String userId;
    private String correlationId;
}
