package se.tink.libraries.application;

public enum ApplicationType {
    SWITCH_MORTGAGE_PROVIDER("switch-mortgage-provider"),
    OPEN_SAVINGS_ACCOUNT("open-savings-account"),
    RESIDENCE_VALUATION("residence-valuation");

    public static final String DOCUMENTED =
            "switch-mortgage-provider,open-savings-account,residence-valuation";

    private String scheme;

    ApplicationType(String scheme) {
        this.scheme = scheme;
    }

    @Override
    public String toString() {
        return scheme;
    }

    public static ApplicationType fromScheme(String scheme) {
        if (scheme != null) {
            for (ApplicationType type : ApplicationType.values()) {
                if (scheme.equalsIgnoreCase(type.scheme)) {
                    return type;
                }
            }
        }
        return null;
    }
}
