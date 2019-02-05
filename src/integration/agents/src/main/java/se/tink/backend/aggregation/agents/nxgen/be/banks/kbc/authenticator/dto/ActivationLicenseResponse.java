package se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.authenticator.dto;

import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.dto.HeaderResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.dto.TypeValuePair;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ActivationLicenseResponse extends HeaderResponse {
    private TypeValuePair challenge;
    private TypeValuePair data;
    private TypeValuePair initialVectorData;
    private TypeValuePair initialVectorSession;
    private TypeValuePair encryptedServerNonces;
    private TypeValuePair encryptedServerPublicKey;
    private TypeValuePair deviceId;
    private TypeValuePair staticVector;
    private TypeValuePair initialVectorStaticVector;

    public TypeValuePair getChallenge() {
        return challenge;
    }

    public TypeValuePair getData() {
        return data;
    }

    public TypeValuePair getInitialVectorData() {
        return initialVectorData;
    }

    public TypeValuePair getInitialVectorSession() {
        return initialVectorSession;
    }

    public TypeValuePair getEncryptedServerNonces() {
        return encryptedServerNonces;
    }

    public TypeValuePair getEncryptedServerPublicKey() {
        return encryptedServerPublicKey;
    }

    public TypeValuePair getDeviceId() {
        return deviceId;
    }

    public TypeValuePair getStaticVector() {
        return staticVector;
    }

    public TypeValuePair getInitialVectorStaticVector() {
        return initialVectorStaticVector;
    }
}
