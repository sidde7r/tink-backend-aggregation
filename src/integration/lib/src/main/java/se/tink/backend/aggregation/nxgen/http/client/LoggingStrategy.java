package se.tink.backend.aggregation.nxgen.http.client;

public enum LoggingStrategy {
    /** Logging the "old-way" using LoggingFilter */
    DEFAULT,
    /**
     * Logging in experimental manner - splitting the logging phase to post executor and a
     * ResponseLoggingFilter
     */
    EXPERIMENTAL,
    /**
     * Disabling the logging. Useful for providing logging externally (e.g. in custom phases due to
     * encryption of message bodies)
     */
    DISABLED
}
