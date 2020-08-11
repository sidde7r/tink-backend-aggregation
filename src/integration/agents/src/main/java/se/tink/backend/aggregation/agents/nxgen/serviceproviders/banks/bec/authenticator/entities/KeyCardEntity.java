package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.authenticator.entities;

import lombok.Data;
import lombok.NoArgsConstructor;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Data
@NoArgsConstructor
public class KeyCardEntity {
    private String keycardNo;
    private String nemidChallenge;
}
