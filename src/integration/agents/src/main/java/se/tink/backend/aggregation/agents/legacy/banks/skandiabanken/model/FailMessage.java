package se.tink.backend.aggregation.agents.banks.skandiabanken.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FailMessage {
    @JsonProperty("Header")
    private String header;

    @JsonProperty("Text")
    private String text;

    public String getHeader() {
        return header;
    }

    public String getText() {
        return text != null ? text : "";
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public void setText(String text) {
        this.text = text;
    }
}
