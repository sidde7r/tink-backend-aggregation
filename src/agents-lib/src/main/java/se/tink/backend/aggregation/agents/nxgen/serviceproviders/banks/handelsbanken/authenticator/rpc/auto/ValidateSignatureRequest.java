package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.rpc.auto;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.entities.PdeviceSignContainer;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ValidateSignatureRequest {

    private PdeviceSignContainer pdeviceSignContainer;

    public ValidateSignatureRequest setPdeviceSignContainer(
            PdeviceSignContainer pdeviceSignContainer) {
        this.pdeviceSignContainer = pdeviceSignContainer;
        return this;
    }
}
