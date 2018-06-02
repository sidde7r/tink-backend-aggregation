package se.tink.backend.core.oauth2;

import io.protostuff.Tag;

public class OAuth2ClientProductMetaData {
    @Tag(1)
    private String customCss;

    public String getCustomCss() {
        return customCss;
    }

    public void setCustomCss(String customCss) {
        this.customCss = customCss;
    }
}
