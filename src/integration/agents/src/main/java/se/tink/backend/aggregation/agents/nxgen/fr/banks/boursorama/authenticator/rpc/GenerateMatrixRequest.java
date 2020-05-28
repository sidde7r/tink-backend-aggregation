package se.tink.backend.aggregation.agents.nxgen.fr.banks.boursorama.authenticator.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class GenerateMatrixRequest {
    private String imageset;
    private int maxButton;

    private GenerateMatrixRequest(String imageset, int maxButton) {
        this.imageset = imageset;
        this.maxButton = maxButton;
    }

    public static GenerateMatrixRequest create(String imagset, int maxButton) {
        return new GenerateMatrixRequest(imagset, maxButton);
    }
}
