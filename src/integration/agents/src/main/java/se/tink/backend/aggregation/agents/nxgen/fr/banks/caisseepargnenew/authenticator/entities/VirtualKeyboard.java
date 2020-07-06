package se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargnenew.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class VirtualKeyboard {

    @JsonProperty("externalRestMediaApiUrl")
    private String externalRestMediaApiUrl;

    @JsonProperty("width")
    private int width;

    @JsonProperty("base64")
    private boolean base64;

    @JsonProperty("audio")
    private boolean audio;

    @JsonProperty("secure")
    private boolean secure;

    @JsonProperty("height")
    private int height;
}
