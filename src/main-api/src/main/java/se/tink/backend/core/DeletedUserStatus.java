package se.tink.backend.core;

public enum DeletedUserStatus {
    // The user is in progress of being deleted.
    IN_PROGRESS,
    // The user has been fully deleted from all systems.
    COMPLETED,
    // An error occurred in the deletion process and the user may not have been fully deleted.
    FAILED
}

