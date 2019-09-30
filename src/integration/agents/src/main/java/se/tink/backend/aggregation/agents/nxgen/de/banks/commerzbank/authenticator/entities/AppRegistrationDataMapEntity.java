package se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.CommerzbankConstants.AppRegistration;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.CommerzbankConstants.Values;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AppRegistrationDataMapEntity {
    @JsonProperty("appInstallation.deviceDescription")
    private String appInstallationDeviceDescription = AppRegistration.DEVICE_DESCRIPTION;

    @JsonProperty("appInstallation.osVersion")
    private String appInstallationOsVersion = Values.OS_VERSION;

    @JsonProperty("appProfilesKey.description")
    private String appProfilesKeyDescription = AppRegistration.DEVICE_DESCRIPTION;
}
