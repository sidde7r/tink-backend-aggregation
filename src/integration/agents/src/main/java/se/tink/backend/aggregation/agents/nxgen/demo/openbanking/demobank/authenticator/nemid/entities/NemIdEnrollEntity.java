package se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.authenticator.nemid.entities;

import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.nemid.NemIdCodeAppPollResponse;

@JsonObject
public class NemIdEnrollEntity {
    private String codeappSerialNo;
    private String responseSignature;
    private String userResponse;
    private String mobileCode;
    private NemIdSecurityDevice securityDevice;

    // Empty constructor required for serialization
    public NemIdEnrollEntity() {}

    public NemIdEnrollEntity(
            NemIdCodeAppPollResponse response, String code, NemIdSecurityDevice securityDevice) {
        this.codeappSerialNo = response.getCodeAppSerialNumber();
        this.responseSignature = response.getPayload().getSignedResponse();
        this.userResponse = response.getPayload().getResponse();
        this.mobileCode = code;
        this.securityDevice = securityDevice;
    }
}
