package se.tink.backend.aggregation.nxgen.http.client;

public enum LoggingScope {
    /** Standard HTTP traffic logs in AAP format */
    HTTP_AAP,
    /**
     * HTTP traffic logs in JSON format with a fixed structure, intended to be analyzed with Athena.
     * NOTE: this scope requires {@link LoggingStrategy#EXPERIMENTAL} flag to work
     */
    HTTP_JSON,
}
