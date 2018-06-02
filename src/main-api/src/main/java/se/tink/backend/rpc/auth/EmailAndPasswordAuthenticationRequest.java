package se.tink.backend.rpc.auth;

import io.protostuff.Tag;

public class EmailAndPasswordAuthenticationRequest {
    @Tag(1)
    private String email;
    @Tag(2)
    private String password;
    @Tag(3)
    private String market;

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setMarket(String market) {
        this.market = market;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getMarket() {
        return market;
    }
}
