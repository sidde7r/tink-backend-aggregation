package se.tink.backend.rpc;

import io.protostuff.Tag;

public class AuthenticatedRegisterRequest {
    @Tag(1)
    private String email;
    @Tag(2)
    private String locale;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }
}
