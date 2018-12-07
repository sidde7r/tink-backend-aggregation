package se.tink.libraries.log.legacy;

/**
 * Tink implementation of logging. Must be thread-safe.
 */
@SuppressWarnings("rawtypes")
public class LogUtils extends se.tink.libraries.log.LogUtils {

    public LogUtils(Class clazz) {
        super(clazz);
    }

    public void error(String userId, String credentialsId, String message) {
        log.error("[userId:" + userId + " credentialsId:" + credentialsId + "] " + message);
    }

    public void warn(String userId, String credentialsId, String message) {
        log.warn("[userId:" + userId + " credentialsId:" + credentialsId + "] " + message);
    }

    public void info(String userId, String credentialsId, String message) {
        log.info("[userId:" + userId + " credentialsId:" + credentialsId + "] " + message);
    }

    public boolean isDebugEnabled() {
        return log.isDebugEnabled();
    }

    public boolean isTraceEnabled() {
        return log.isTraceEnabled();
    }

    public void trace(String message) {
        log.trace(message);
    }

    public void info(String message, Throwable e) {
        log.info(message, e);
    }

}
