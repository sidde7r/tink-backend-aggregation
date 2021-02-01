package se.tink.backend.agents.rpc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

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

    public SelectOption(String text, String value, String iconUrl) {
        this.text = text;
        this.value = value;
        this.iconUrl = iconUrl;
    }

    public String getText() {
        return text;
    }

    public String getValue() {
        return value;
    }

    public String getIconUrl() {
        return iconUrl;
    }
}
