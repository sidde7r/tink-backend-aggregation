package se.tink.backend.core.oauth2;

import io.protostuff.Tag;

public class OAuth2AuthorizeResponse {
    @Tag(1)
    private String code;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
