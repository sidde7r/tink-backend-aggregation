package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.authenticator.entities;

import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Data
@NoArgsConstructor
public class SecondFactorOperationsEntity {
    private List<String> secondFactorOptions;
    private KeyCardEntity keycard;
}
