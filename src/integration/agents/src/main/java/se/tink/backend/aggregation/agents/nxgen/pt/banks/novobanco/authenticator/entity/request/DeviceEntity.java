package se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.authenticator.entity.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.NovoBancoConstants.FieldValues;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class DeviceEntity {
    @JsonProperty("Resolution")
    private int resolution = 1;

    @JsonProperty("Latitude")
    private double latitude = FieldValues.LATITUDE;

    @JsonProperty("SecureElement")
    private boolean secureElement = true;

    @JsonProperty("OSVersion")
    private String osVersion = FieldValues.OS_VERSION;

    @JsonProperty("Id")
    private String id = FieldValues.DEFAULT_DEVICE_ID;

    @JsonProperty("Longitude")
    private double longitude = FieldValues.LONGITUDE;

    @JsonProperty("OS")
    private String you = FieldValues.OS;

    @JsonProperty("Jailbroken")
    private boolean jailbroken = false;

    @JsonProperty("Name")
    private String yam = FieldValues.DEVICE_NAME;

    @JsonProperty("Model")
    private String model = FieldValues.MODEL;
}
