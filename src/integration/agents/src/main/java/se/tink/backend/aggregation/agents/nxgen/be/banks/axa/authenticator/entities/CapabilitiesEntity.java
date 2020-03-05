package se.tink.backend.aggregation.agents.nxgen.be.banks.axa.authenticator.entities;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.util.Collections;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class CapabilitiesEntity {

    private boolean audioAcquisitionSupported;
    private boolean dyadicPresent;
    private boolean faceIdKeyBioProtectionSupported;
    private boolean fidoClientPresent;
    private boolean fingerPrintSupported;
    private String hostProvidedFeatures = "19";
    private boolean imageAcquisitionSupported;
    private List installedPlugins = Collections.EMPTY_LIST;
    private boolean persistentKeysSupported;
}
