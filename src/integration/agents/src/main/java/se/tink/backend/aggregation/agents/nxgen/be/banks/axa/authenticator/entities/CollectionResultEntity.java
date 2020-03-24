package se.tink.backend.aggregation.agents.nxgen.be.banks.axa.authenticator.entities;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.fasterxml.jackson.databind.node.ObjectNode;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class CollectionResultEntity {

    private Content content;
    private Metadata metadata;

    public CollectionResultEntity(String deviceId, String deviceName) {
        this.content = new Content(deviceId, deviceName);
        this.metadata = new Metadata();
    }

    public Content getContent() {
        return content;
    }

    public void setContent(Content content) {
        this.content = content;
    }

    public Metadata getMetadata() {
        return metadata;
    }

    public void setMetadata(Metadata metadata) {
        this.metadata = metadata;
    }

    @JsonObject
    @JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
    public static class Content {
        private CapabilitiesEntity capabilities;
        private CollectorStateEntity collectorState;
        private DeviceDetailsEntity deviceDetails;
        private ObjectNode localEnrollments = new ObjectMapper().createObjectNode();

        public Content(String deviceId, String deviceName) {
            this.capabilities = new CapabilitiesEntity();
            this.collectorState = new CollectorStateEntity();
            this.deviceDetails = new DeviceDetailsEntity(deviceId, deviceName);
        }
    }

    @JsonObject
    public static class Metadata {
        private Long timestamp = System.currentTimeMillis();
    }
}
