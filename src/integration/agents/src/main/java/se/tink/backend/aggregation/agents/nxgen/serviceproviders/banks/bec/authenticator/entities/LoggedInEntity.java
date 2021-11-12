package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.authenticator.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoggedInEntity {
    private String lastUsed;
    private String username;
    private String bankReference;
    private String scaToken;
}
