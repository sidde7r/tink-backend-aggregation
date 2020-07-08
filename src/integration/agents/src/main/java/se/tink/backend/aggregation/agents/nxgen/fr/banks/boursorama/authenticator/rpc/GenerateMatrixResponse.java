package se.tink.backend.aggregation.agents.nxgen.fr.banks.boursorama.authenticator.rpc;

import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.boursorama.authenticator.entity.KeymapEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class GenerateMatrixResponse {
    private KeymapEntity keymap;
    private String matrixRandomChallenge;
}
