package se.tink.backend.aggregation.agents.utils.authentication.encap.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class SamlResultEntity {
    private int responseType;
    private String responseContent;
    private PluginEntity plugin;

    public int getResponseType() {
        return responseType;
    }

    public String getResponseContent() {
        return responseContent;
    }

    public PluginEntity getPlugin() {
        return plugin;
    }
}
