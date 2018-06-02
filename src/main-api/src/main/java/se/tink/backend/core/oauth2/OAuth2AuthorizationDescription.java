package se.tink.backend.core.oauth2;

import io.protostuff.Tag;

import java.util.List;

public class OAuth2AuthorizationDescription {
    @Tag(3)
    private String clientIconUrl;
    @Tag(1)
    private String clientName;
    @Tag(2)
    private String clientUrl;
    @Tag(4)
    private List<String> scopesDescriptions;
    @Tag(5)
    private boolean embeddedAllowed;

    public OAuth2AuthorizationDescription() {

    }

    public String getClientIconUrl() {
        return clientIconUrl;
    }

    public String getClientName() {
        return clientName;
    }

    public String getClientUrl() {
        return clientUrl;
    }

    public List<String> getScopesDescriptions() {
        return scopesDescriptions;
    }

    public void setClientIconUrl(String clientIconUrl) {
        this.clientIconUrl = clientIconUrl;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public void setClientUrl(String clientUrl) {
        this.clientUrl = clientUrl;
    }

    public void setScopesDescriptions(List<String> scopesDescriptions) {
        this.scopesDescriptions = scopesDescriptions;
    }

    public boolean isEmbeddedAllowed() {
        return embeddedAllowed;
    }

    public void setEmbeddedAllowed(boolean embeddedAllowed) {
        this.embeddedAllowed = embeddedAllowed;
    }
}
