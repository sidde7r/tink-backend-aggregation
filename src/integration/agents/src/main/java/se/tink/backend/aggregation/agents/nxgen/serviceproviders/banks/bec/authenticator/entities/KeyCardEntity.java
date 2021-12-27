package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.authenticator.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class KeyCardEntity {
    private String keycardNo;
    private String nemidChallenge;
}
