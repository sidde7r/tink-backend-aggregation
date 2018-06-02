package se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.authenticator.dto;

import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.dto.HeaderResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.dto.TypeValuePair;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class EnrollDeviceRoundTwoResponse extends HeaderResponse {
    private TypeValuePair activationPassword;
    private TypeValuePair deviceId;
    private TypeValuePair accessNumber;
    private TypeValuePair tokenId;

    public TypeValuePair getActivationPassword() {
        return activationPassword;
    }

    public TypeValuePair getDeviceId() {
        return deviceId;
    }

    public TypeValuePair getAccessNumber() {
        return accessNumber;
    }

    public TypeValuePair getTokenId() {
        return tokenId;
    }
}
