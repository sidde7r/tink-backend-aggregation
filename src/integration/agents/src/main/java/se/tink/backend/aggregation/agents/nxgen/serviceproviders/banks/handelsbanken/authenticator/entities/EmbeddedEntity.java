package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import se.tink.backend.aggregation.annotations.JsonObject;

@NoArgsConstructor
@Getter
@JsonObject
public class EmbeddedEntity {
    @JsonProperty("analytics-config")
    private AnalyticsConfigEntity analyticsConfig;

    @JsonProperty("user-info")
    private UserInfoEntity userInfo;
}
