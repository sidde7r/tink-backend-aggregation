package se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.model.entity.tlc.device;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import se.tink.backend.aggregation.annotations.JsonObject;

@Builder
@JsonObject
public class CollectionResultEntity {

    @JsonProperty("content")
    private final ContentEntity collectorState;

    @JsonProperty("metadata")
    private final MetadataRequest metadata;

    @JsonIgnore
    public static CollectionResultEntity getDefault() {
        return CollectionResultEntity.builder()
                .collectorState(ContentEntity.getDefault())
                .metadata(MetadataRequest.createInstance())
                .build();
    }

    @JsonObject
    @AllArgsConstructor
    public static class MetadataRequest {
        private final long timestamp;

        @JsonIgnore
        public static MetadataRequest createInstance() {
            return new MetadataRequest(System.currentTimeMillis());
        }
    }
}
