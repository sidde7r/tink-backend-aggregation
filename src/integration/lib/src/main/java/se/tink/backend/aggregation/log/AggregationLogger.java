package se.tink.backend.aggregation.log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extension of AggregationLogger to provide custom formatter for Credentials
 *
 * <p>The methods for (String, String, String) and similar are deprecated, and should be converted
 * to type-safe alternatives.
 *
 * @deprecated Please depend on slf4j directly instead of AggregationLoggger.
 *     https://tink.slack.com/archives/CB12SB8DV/p1592400385429200
 *     https://tink.slack.com/archives/C239US7C5/p1525431375000316
 */
@Deprecated
public class AggregationLogger {
    protected Logger log;

    public AggregationLogger(Class clazz) {
        this.log = LoggerFactory.getLogger(clazz);
    }

    public void debug(String message) {
        log.debug(message);
    }

    public void error(String message) {
        log.error(message);
    }

    public void error(String message, Throwable e) {
        log.error(message, e);
    }

    public void info(String message) {
        log.info(message);
    }

    public void info(String message, Throwable e) {
        log.info(message, e);
    }

    public void warn(String message) {
        log.warn(message);
    }

    public void warn(String message, Throwable e) {
        log.warn(message, e);
    }
}
