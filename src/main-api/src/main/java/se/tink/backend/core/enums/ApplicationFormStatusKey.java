package se.tink.backend.core.enums;

public enum ApplicationFormStatusKey {
    CREATED,
    COMPLETED,
    IN_PROGRESS,
    ERROR,
    DISQUALIFIED,
    AUTO_SAVED;

    public static final String DOCUMENTED = "CREATED, COMPLETED, IN_PROGRESS, ERROR, DISQUALIFIED, AUTO_SAVED";
}
