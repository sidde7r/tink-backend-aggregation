package se.tink.backend.agents.rpc;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SelectOption {

    private final String text;
    private final String value;
    private final String iconUrl;

    public SelectOption(String text, String value) {
        this.text = text;
        this.value = value;
        this.iconUrl = null;
    }

    @JsonCreator
    public SelectOption(
            @JsonProperty("text") String text,
            @JsonProperty("value") String value,
            @JsonProperty("iconUrl") String iconUrl) {
        this.text = text;
        this.value = value;
        this.iconUrl = iconUrl;
    }
}
