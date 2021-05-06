package se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.model.entity.tlc.device;

import com.fasterxml.jackson.databind.PropertyNamingStrategy.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Builder;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@JsonNaming(value = SnakeCaseStrategy.class)
@Builder
public class CapabilitiesEntity {
    private final boolean audioAcquisitionSupported;
    private final boolean fingerPrintSupported;
    private final boolean imageAcquisitionSupported;
    private final boolean persistentKeysSupported;
    private final boolean faceIdKeyBioProtectionSupported;
    private final boolean fidoClientPresent;
    private final boolean dyadicPresent;
    private final Object[] installedPlugins;
    private final String hostProvidedFeatures;

    public static CapabilitiesEntity getDefault() {
        return CapabilitiesEntity.builder()
                .audioAcquisitionSupported(true)
                .dyadicPresent(false)
                .faceIdKeyBioProtectionSupported(false)
                .fidoClientPresent(false)
                .fingerPrintSupported(true)
                .hostProvidedFeatures("19")
                .imageAcquisitionSupported(true)
                .installedPlugins(new Object[0])
                .persistentKeysSupported(true)
                .build();
    }
}
