package se.tink.backend.aggregation.agents.nxgen.fr.banks.boursorama.authenticator.rpc;

import java.util.Map;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.boursorama.authenticator.entity.KeymapEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class GenerateMatrixResponse {
    private String expire;
    private KeymapEntity keymap;
    private String matrixRandomChallenge;
    private String token;

    public Map<String, String> getKeys() {
        return keymap.getKeys();
    }

    public String getMatrixRandomChallenge() {
        return matrixRandomChallenge;
    }
}
