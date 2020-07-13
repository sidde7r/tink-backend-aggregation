package se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargnenew.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Context {
    @JsonProperty("OPS_GENERIQUE")
    private OPSGENERIQUE oPSGENERIQUE;
}
