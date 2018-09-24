package se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Map;
import java.util.Optional;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class WebAPI2Entity {
    private String authBusinessContextRoot;
    private String authAccessTokenURL;
    private Map<String, String> entryPoints;

    public String getAuthBusinessContextRoot() {
        return authBusinessContextRoot;
    }

    public String getAuthAccessTokenURL() {
        return authAccessTokenURL;
    }

    @JsonIgnore
    public String getEntryPoint(String entryPoint) {
        return Optional.ofNullable(entryPoints).map(m -> m.get(entryPoint)).orElse("");
    }
}
