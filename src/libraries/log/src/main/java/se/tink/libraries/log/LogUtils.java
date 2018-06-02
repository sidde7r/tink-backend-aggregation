package se.tink.libraries.log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tink implementation of logging. Must be thread-safe.
 */
@SuppressWarnings("rawtypes")
public class LogUtils {
    protected Logger log;

    public LogUtils(Class clazz) {
        this.log = LoggerFactory.getLogger(clazz);
    }

    public void debug(String message) {
        log.debug(message);
    }

    public void debug(String message, Throwable e) {
        log.debug(message, e);
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

    public void trace(String message) {
        log.trace(message);
    }

    public void trace(String message, Throwable e) {
        log.trace(message, e);
    }

    public void warn(String message) {
        log.warn(message);
    }

    public void warn(String message, Throwable e) {
        log.warn(message, e);
    }

    public boolean isTraceEnabled() {
        return log.isTraceEnabled();
    }
}
