package se.tink.libraries.auth;

public enum HttpAuthenticationMethod {
    SESSION("Session"),
    BASIC("Basic"),
    BEARER("Bearer"),
    FACEBOOK("Facebook"),
    NON_VALID(null),
    TOKEN("Token");

    private String method;

    HttpAuthenticationMethod(String method) {
        this.method = method;
    }

    public boolean isValid() {
        if (method == null) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return method;
    }

    public String getMethod() {
        return method;
    }

    public static HttpAuthenticationMethod fromMethod(String method) {
        if (method != null) {
            for (HttpAuthenticationMethod type : HttpAuthenticationMethod.values()) {
                if (method.equalsIgnoreCase(type.method)) {
                    return type;
                }
            }
        }
        return NON_VALID;
    }
}
