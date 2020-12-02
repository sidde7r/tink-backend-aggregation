package se.tink.libraries.jersey.utils;

/** Indicates an entity that can be safely log */
public interface SafelyLoggable {

    /** returns entity representation not containing user sensitive data */
    String toSafeString();
}
