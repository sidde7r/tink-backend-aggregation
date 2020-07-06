package se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargnenew.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class ImageItem {

    @JsonProperty("uri")
    private String uri;

    @JsonProperty("value")
    private String value;
}
