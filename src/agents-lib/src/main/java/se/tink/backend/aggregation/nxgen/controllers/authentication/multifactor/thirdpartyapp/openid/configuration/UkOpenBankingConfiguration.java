package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.configuration;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Map;
import org.assertj.core.util.Preconditions;
import se.tink.backend.aggregation.agents.utils.encoding.EncodingUtils;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class UkOpenBankingConfiguration {

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

    @JsonIgnore
    public void validate() {

        Preconditions.checkNotNullOrEmpty(rootCAData);
        Preconditions.checkNotNullOrEmpty(rootCAPassword);
        softwareStatements.forEach((k, v) -> v.validate());
    }
}
