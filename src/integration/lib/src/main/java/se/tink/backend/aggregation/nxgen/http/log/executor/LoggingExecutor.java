package se.tink.backend.aggregation.nxgen.http.log.executor;

/**
 * This is a class responsible for performing the actual logging of common request/responses model
 * objects
 */
public interface LoggingExecutor {

    void log(RequestLogEntry entry);

    void log(ResponseLogEntry entry);
}
