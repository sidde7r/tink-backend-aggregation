package se.tink.backend.rpc.oauth;

import se.tink.libraries.http.annotations.validation.StringNotNullOrEmpty;

/**
 * Temporary request object for partners who are interested in the Growth offering. This will be removed when we have
 * a portal for Growth partners (Tink console).
 */
public class OAuthPartnerRequest {

    @StringNotNullOrEmpty
    private String token;
    @StringNotNullOrEmpty
    private String name;
    @StringNotNullOrEmpty
    private String email;
    private String description;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
