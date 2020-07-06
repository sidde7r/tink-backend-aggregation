package se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargnenew.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class Parameters {

    @JsonProperty("SAMLRequest")
    private String sAMLRequest;
}
