package se.tink.backend.aggregation.nxgen.http.log.executor.json.entity;

import lombok.Builder;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@Getter
@JsonObject
@Builder
public class HttpJsonLogMetaEntity {

    private final String providerName;
    private final String agentName;
    private final String appId;
    private final String clusterId;
    private final String credentialsId;
    private final String userId;
    private final String requestId;
    private final String operation;
}
