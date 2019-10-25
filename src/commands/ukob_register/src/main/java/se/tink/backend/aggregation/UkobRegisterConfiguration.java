package se.tink.backend.aggregation;

import se.tink.backend.aggregation.agents.utils.encoding.EncodingUtils;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.configuration.SoftwareStatement;

@JsonObject
public class UkobRegisterConfiguration {

    private SoftwareStatement softwareStatement;
    private String rootCAData;
    private String rootCAPassword;

    public SoftwareStatement getSoftwareStatement() {
        return softwareStatement;
    }

    public byte[] getRootCAData() {
        return EncodingUtils.decodeBase64String(rootCAData);
    }

    public String getRootCAPassword() {
        return rootCAPassword;
    }
}
