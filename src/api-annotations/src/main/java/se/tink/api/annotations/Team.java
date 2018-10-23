package se.tink.api.annotations;

/**
 * Make sure that there are alerts defined for each team if you add or remove one.
 */
public enum Team {
    @Deprecated
    DATA,
    INTEGRATION,
    PFM
}
