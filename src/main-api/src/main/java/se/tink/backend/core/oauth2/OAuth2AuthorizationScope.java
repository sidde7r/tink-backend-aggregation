package se.tink.backend.core.oauth2;

import io.protostuff.Tag;

public class OAuth2AuthorizationScope {
    @Tag(2)
    private String description;
    @Tag(3)
    private String group;
    @Tag(1)
    private String type;

    public OAuth2AuthorizationScope() {

    }

    public OAuth2AuthorizationScope(String type, String description, String group) {
        this.type = type;
        this.description = description;
        this.group = group;
    }

    public String getDescription() {
        return description;
    }

    public String getGroup() {
        return group;
    }

    public String getType() {
        return type;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public void setType(String type) {
        this.type = type;
    }
}