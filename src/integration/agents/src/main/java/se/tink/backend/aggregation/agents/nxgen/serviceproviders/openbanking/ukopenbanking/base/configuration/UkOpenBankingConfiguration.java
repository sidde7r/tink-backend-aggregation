package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.configuration;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Map;
import java.util.Optional;
import org.assertj.core.util.Preconditions;
import se.tink.backend.aggregation.agents.utils.encoding.EncodingUtils;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.configuration.SoftwareStatement;

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

    public Optional<SoftwareStatement> getSoftwareStatement(String name) {
        return Optional.ofNullable(softwareStatements.getOrDefault(name, null));
    }

    @JsonIgnore
    public void validate() {
        Preconditions.checkNotNullOrEmpty(rootCAData);
        Preconditions.checkNotNullOrEmpty(rootCAPassword);
    }
}
