package se.tink.libraries.http.utils;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.Status;
import org.slf4j.Logger;

public class WebApplicationErrorUtils {

    /**
     * Raise an error to the user.
     *
     * @param responseCode the response code the user will see.
     */
    public static void error(final Status responseCode) {
        throw new WebApplicationException(responseCode);
    }

    /**
     * Log and present an error to the user.
     *
     * @param logger the logger to which the log message should be written. Severity will be based
     *     on responseCode.
     * @param responseCode the response code the user will receive.
     * @param logMessage the message that will be logged.
     */
    public static void error(final Logger logger, final Status responseCode, String logMessage) {
        final String enrichedLogMessage =
                String.format("[HTTP=%d] %s", responseCode.getStatusCode(), logMessage);

        if (400 <= responseCode.getStatusCode() && responseCode.getStatusCode() < 500)
            logger.warn(enrichedLogMessage);
        else if (500 <= responseCode.getStatusCode() && responseCode.getStatusCode() < 600)
            logger.error(enrichedLogMessage);
        else logger.debug(enrichedLogMessage);

        throw new WebApplicationException(responseCode);
    }

    /**
     * Log and present an error to the user.
     *
     * @param logger the logger to which the log message should be written. Severity will be based
     *     on responseCode.
     * @param responseCode the response code the user will receive.
     * @param logMessage the message that will be logged.
     * @param e an exception to be logged.
     */
    public static void error(Logger logger, Status responseCode, String logMessage, Throwable e) {
        final String enrichedLogMessage =
                String.format("[HTTP=%d] %s", responseCode.getStatusCode(), logMessage);

        if (400 <= responseCode.getStatusCode() && responseCode.getStatusCode() < 500)
            logger.warn(enrichedLogMessage, e);
        else if (500 <= responseCode.getStatusCode() && responseCode.getStatusCode() < 600)
            logger.error(enrichedLogMessage, e);
        else logger.debug(enrichedLogMessage, e);

        throw new WebApplicationException(responseCode);
    }
}
