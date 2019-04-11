package se.tink.backend.aggregation.agents.nxgen.fr.banks.societegenerale.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LoginGridData {

    private String crypto;

    @JsonProperty("grid")
    private List<Integer> oneTimePad;

    private int nbrows;
    private int nbcols;

    public String getCrypto() {
        return crypto;
    }

    public List<Integer> getOneTimePad() {
        return oneTimePad;
    }
}
