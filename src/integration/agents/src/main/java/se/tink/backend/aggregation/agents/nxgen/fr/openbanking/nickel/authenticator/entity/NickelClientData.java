package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.nickel.authenticator.entity;

import static se.tink.backend.aggregation.agents.nxgen.fr.openbanking.nickel.NickelConstants.APP_TYPE;
import static se.tink.backend.aggregation.agents.nxgen.fr.openbanking.nickel.NickelConstants.APP_VERSION;
import static se.tink.backend.aggregation.agents.nxgen.fr.openbanking.nickel.NickelConstants.DEVICE_NAME;
import static se.tink.backend.aggregation.agents.nxgen.fr.openbanking.nickel.NickelConstants.DEVICE_TYPE;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Data
public class NickelClientData {

    private NickelApp app;
    private NickelDevice device;

    @JsonIgnore
    public NickelClientData(String deviceId) {
        device = NickelDevice.builder().id(deviceId).name(DEVICE_NAME).type(DEVICE_TYPE).build();
        app = NickelApp.builder().type(APP_TYPE).version(APP_VERSION).build();
    }
}
