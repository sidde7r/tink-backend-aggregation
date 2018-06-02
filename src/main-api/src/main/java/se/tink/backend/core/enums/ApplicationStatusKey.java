package se.tink.backend.core.enums;


public enum ApplicationStatusKey {
    // Internal
    CREATED,
    IN_PROGRESS,
    EXPIRED,
    DELETED,
    ERROR,
    DISQUALIFIED,
    COMPLETED,
    SIGNED, // Important that this is declared last in the internal group, since it's ordinal is used for logics.
    
    // External. Important that these are declared _after_ the internal statuses, since the ordinal is used for logics.
    SUPPLEMENTAL_INFORMATION_REQUIRED, // Supplemental information is required. Either the provider will contact the user, or the user has to act.
    REJECTED, // The application was rejected by the provider.
    APPROVED, // The application was approved by the provider. The user can still choose to not accept the final terms and abort.
    ABORTED, // The application aborted by user. Either after not accepting the providers final terms, or just withdrew the application.
    EXECUTED; // The application was approved by the provider, accepted by the user, and finally executed.

    public static final String DOCUMENTED =
            // Internal
            "CREATED, " +
            "IN_PROGRESS, " +
            "EXPIRED, " +
            "ERROR, " +
            "DISQUALIFIED, " +
            "COMPLETED, " +
            "SIGNED," +
            // External
            "SUPPLEMENTAL_INFORMATION_REQUIRED, " +
            "REJECTED, " +
            "APPROVED, " +
            "ABORTED, " +
            "EXECUTED";

    public boolean isUserModifiable() {
        if (this.equals(EXPIRED) || this.equals(DELETED)) {
            return false;
        }

        if (this.ordinal() >= SIGNED.ordinal()) {
            return false;
        }

        return true;
    }


}
