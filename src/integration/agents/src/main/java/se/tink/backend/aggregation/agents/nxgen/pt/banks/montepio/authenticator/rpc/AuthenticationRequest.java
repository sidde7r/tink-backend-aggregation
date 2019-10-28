package se.tink.backend.aggregation.agents.nxgen.pt.banks.montepio.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.montepio.MontepioConstants;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.montepio.authenticator.entities.DeviceInfoEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AuthenticationRequest {

    @JsonProperty("Alias")
    private String username;

    @JsonProperty("ClientType")
    private String clientType = MontepioConstants.FieldValues.CLIENT_TYPE;

    @JsonProperty("CredentialType")
    private int credentialType = MontepioConstants.FieldValues.CREDENTIAL_TYPE;

    @JsonProperty("DeviceInfo")
    private DeviceInfoEntity deviceInfo = new DeviceInfoEntity();

    @JsonProperty("Ip")
    private String ip = MontepioConstants.FieldValues.PSU_IP;

    @JsonProperty("Lat")
    private String lat = MontepioConstants.FieldValues.LATITUDE;

    @JsonProperty("Lon")
    private String lon = MontepioConstants.FieldValues.LONGTITUDE;

    @JsonProperty("Password")
    private String maskedPassword;

    @JsonProperty("Positions")
    private List<String> positions = Collections.emptyList();

    public AuthenticationRequest(String username, String maskedPassword) {
        this.username = username;
        this.maskedPassword = maskedPassword;
    }
}
