package se.tink.backend.aggregation.nxgen.controllers.authentication.utils;

public class StrongAuthenticationState {
    private static final String UNIQUE_PREFIX_TPCB = "tpcb_%s";

    private final String state;

    public StrongAuthenticationState(String state) {
        this.state = state;
    }

    public String getState() {
        return this.state;
    }

    public String getSupplementalKey() {
        return String.format(UNIQUE_PREFIX_TPCB, this.state);
    }

    public static String formatSupplementalKey(String key) {
        return String.format(UNIQUE_PREFIX_TPCB, key);
    }

    @Override
    public String toString() {
        return "StrongAuthenticationState{" + "state='" + state + '\'' + '}';
    }
}
