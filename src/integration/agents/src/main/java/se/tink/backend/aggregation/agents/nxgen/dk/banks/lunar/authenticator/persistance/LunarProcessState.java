package se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.persistance;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LunarProcessState {
    private String nemIdParameters;
    private String challenge;
    private String nemIdToken;
    private boolean isAutoAuth;
}
