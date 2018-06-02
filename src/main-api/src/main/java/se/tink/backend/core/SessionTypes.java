package se.tink.backend.core;

public enum SessionTypes {
    MOBILE, WEB, LINK, OAUTH
    // OAUTH will not appear in UserSession.
    // The type was added here to be able to have a type for oauth authorized sessions in AuthenticatedUser
}
