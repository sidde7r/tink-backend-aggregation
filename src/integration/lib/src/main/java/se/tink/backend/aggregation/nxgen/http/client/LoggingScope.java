package se.tink.backend.aggregation.nxgen.http.client;

public enum LoggingScope {
    /** Standard HTTP traffic logs in "AAP like" raw format */
    HTTP_RAW,
    /**
     * HTTP traffic logs in JSON format with a fixed structure, intended to be analyzed with Athena.
     * NOTE: this scope requires {@link LoggingStrategy#EXPERIMENTAL} flag to work
     */
    HTTP_JSON,
}
