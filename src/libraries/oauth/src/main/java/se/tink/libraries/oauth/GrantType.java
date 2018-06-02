package se.tink.libraries.oauth;

public enum GrantType {

    AUTHORIZATION_CODE("authorization_code"),
    REFRESH_TOKEN("refresh_token");

    public static final String DOCUMENTED = "authorization_code, refresh_token";
    private String name;

    GrantType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
