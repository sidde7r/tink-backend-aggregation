package se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.authenticator.rpc;

import java.util.UUID;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.CommerzbankConstants.AppRegistration;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.CommerzbankConstants.Values;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class InitAppRegistrationRequest {
    private String deviceDescription = AppRegistration.DEVICE_DESCRIPTION;
    private String deviceUUID = UUID.randomUUID().toString().toUpperCase();
    private String osType = AppRegistration.OS_TYPE;
    private String osVersion = Values.OS_VERSION;
}
