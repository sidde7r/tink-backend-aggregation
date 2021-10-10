package se.tink.backend.aggregation.configuration.models.configuration;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class S3StorageConfiguration {

    @JsonProperty private boolean enabled = false;

    @JsonProperty private String url;

    @JsonProperty private String region;

    @JsonProperty private String agentDebugBucketName;

    @JsonProperty private String httpJsonDebugBucketName;
}
