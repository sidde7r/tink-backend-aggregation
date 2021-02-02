package se.tink.backend.aggregation.client.provider_configuration.rpc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SelectOption {
    private String text;
    private String value;
    private String iconUrl;

    public String getText() {
        return text;
    }

    public String getValue() {
        return value;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }
}
