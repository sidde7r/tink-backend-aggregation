package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.configuration;

import java.util.Map;
import se.tink.backend.aggregation.agents.utils.encoding.EncodingUtils;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class UKOB {

    private String rootCAData;
    private String rootCAPassword;

    private Map<String, SoftwareStatement> softwareStatements;

    public byte[] getRootCAData() {
        return EncodingUtils.decodeBase64String(rootCAData);
    }

    public String getRootCAPassword() {
        return rootCAPassword;
    }

    public SoftwareStatement getSoftwareStatement(String name) {
        return softwareStatements.get(name);
    }
}
