package se.tink.backend.aggregation.agents.nxgen.mx.banks.citibanamex.authenticator.entity;

import se.tink.backend.aggregation.agents.nxgen.mx.banks.citibanamex.CitiBanaMexConstants.BaseFormRequestValues;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class MobileSdkRequest {

    private int Compromised;

    private int Emulator;

    private String SDK_VERSION;

    private String RSA_ApplicationKey;

    private String TIMESTAMP;

    private String HardwareID;

    public MobileSdkRequest(String rsaApplicationKey, String timestamp, String hardwareId) {
        this.Compromised = BaseFormRequestValues.COMPROMISED;
        this.Emulator = BaseFormRequestValues.EMULATOR;
        this.SDK_VERSION = BaseFormRequestValues.SDK_VERSION;
        this.RSA_ApplicationKey = rsaApplicationKey;
        this.TIMESTAMP = timestamp;
        this.HardwareID = hardwareId;
    }
}
