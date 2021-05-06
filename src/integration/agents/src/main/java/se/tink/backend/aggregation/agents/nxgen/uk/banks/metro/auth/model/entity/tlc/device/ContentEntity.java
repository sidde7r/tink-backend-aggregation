package se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.model.entity.tlc.device;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.PropertyNamingStrategy.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonNaming(value = SnakeCaseStrategy.class)
@JsonObject
@Builder
public class ContentEntity {

    private final DeviceDetailsEntity deviceDetails;
    private final LocationEntity location;
    private final CapabilitiesEntity capabilities;
    private final CollectorStateEntity collectorState;
    private final Object[] installedPackages;
    private final LocalEnrollments localEnrollments;

    @JsonIgnore
    public static ContentEntity getDefault() {
        return ContentEntity.builder()
                .deviceDetails(DeviceDetailsEntity.getDefault())
                .location(new LocationEntity(false))
                .capabilities(CapabilitiesEntity.getDefault())
                .collectorState(CollectorStateEntity.getDefault())
                .installedPackages(new Object[0])
                .localEnrollments(LocalEnrollments.builder().build())
                .build();
    }

    @JsonObject
    @JsonInclude(Include.NON_NULL)
    @Builder
    public static class LocalEnrollments {
        private final Pin pin;
    }

    @JsonObject
    @AllArgsConstructor
    public static class Pin {
        private final String registrationStatus;
        private final String validationStatus;
    }
}
