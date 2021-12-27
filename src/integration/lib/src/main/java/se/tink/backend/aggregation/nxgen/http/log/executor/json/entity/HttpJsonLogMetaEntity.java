package se.tink.backend.aggregation.nxgen.http.log.executor.json.entity;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
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
