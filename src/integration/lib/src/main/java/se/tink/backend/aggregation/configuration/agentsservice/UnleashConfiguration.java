package se.tink.backend.aggregation.configuration.agentsservice;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Slf4j
public class UnleashConfiguration {
    @JsonProperty private String baseApiUrl;
    @JsonProperty private boolean developerMode = false;

    public String getBaseApiUrl() {
        if (developerMode) {
            log.info(
                    "Unleash configuration has been initialized in DEVELOPER_MODE. The base API URL is `{}`. "
                            + "In order to use all of the functionalities locally, please follow the instruction from `tink-backend/src/unleash/README.MD`",
                    baseApiUrl);
        }
        return baseApiUrl;
    }
}
