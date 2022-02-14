package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.entity;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import se.tink.backend.aggregation.annotations.JsonObject;

@AllArgsConstructor
@NoArgsConstructor
@JsonObject
@EqualsAndHashCode
public class PsuCredentialsValues {

    private String productCode;
    private List<CredentialDetailValue> credentialsDetails;
}
