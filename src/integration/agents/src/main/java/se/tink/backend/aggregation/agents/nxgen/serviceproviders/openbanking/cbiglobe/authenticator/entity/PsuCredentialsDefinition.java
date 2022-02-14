package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.entity;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import se.tink.backend.aggregation.annotations.JsonObject;

@AllArgsConstructor
@NoArgsConstructor
@JsonObject
@Getter
public class PsuCredentialsDefinition {

    private String aspspProductCode;
    private List<CredentialDetailDefinition> credentialsDetails;
}
