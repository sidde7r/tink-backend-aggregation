package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.secrets;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.annotations.Secret;
import se.tink.backend.aggregation.annotations.SensitiveSecret;
import se.tink.backend.aggregation.configuration.agents.ClientConfiguration;

@JsonObject
@Getter
public class StarlingSecrets implements ClientConfiguration {

    // TODO: Migrate secrets to properties called clientId and clientSecret
    @JsonProperty(required = true)
    @Secret
    private String aisClientId;

    @JsonProperty(required = true)
    @SensitiveSecret
    private String aisClientSecret;

    @JsonProperty(required = false)
    @SensitiveSecret
    private String keyUid;
}
