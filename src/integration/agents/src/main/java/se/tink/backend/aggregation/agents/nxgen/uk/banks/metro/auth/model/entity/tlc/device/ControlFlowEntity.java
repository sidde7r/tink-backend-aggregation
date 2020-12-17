package se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.model.entity.tlc.device;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.vavr.collection.Array;
import java.util.List;
import java.util.Objects;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@Getter
@JsonObject
public class ControlFlowEntity {
    private List<MethodEntity> methods;

    private List<AssertionEntity> assertions;

    public MethodEntity findMethod(String methodType) {
        Objects.requireNonNull(methodType);
        return Array.ofAll(methods)
                .find(methodEntity -> methodEntity.getType().equals(methodType))
                .getOrElseThrow(() -> new IllegalStateException("Could not find proper method!"));
    }

    @Getter
    @JsonObject
    public static class MethodEntity {
        @JsonProperty("assertion_id")
        private String assertionId;

        private String type;

        private List<ChannelEntity> channels;

        @Getter
        @JsonObject
        public static class ChannelEntity {
            @JsonProperty("assertion_id")
            private String assertionId;
        }
    }

    @Getter
    @JsonObject
    public static class AssertionEntity {

        @JsonProperty("assertion_id")
        private String assertionId;
    }
}
