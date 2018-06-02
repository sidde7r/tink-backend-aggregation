package se.tink.backend.common.utils;

import java.util.UUID;
import se.tink.libraries.uuid.UUIDUtils;

/**
 * Tink implementation of logging. Must be thread-safe.
 */
@SuppressWarnings("rawtypes")
public class LogUtils extends se.tink.libraries.log.LogUtils {
    public LogUtils(Class clazz) {
        super(clazz);
    }

    public void debug(String userId, String message) {
        log.debug("[userId:" + userId + "] " + message);
    }

    public void debug(String userId, String credentialsId, String message) {
        log.debug("[userId:" + userId + " credentialsId:" + credentialsId + "] " + message);
    }

    public void error(String userId, String message) {
        log.error("[userId:" + userId + "] " + message);
    }

    public void error(String userId, String message, Throwable e) {
        log.error("[userId:" + userId + "] " + message, e);
    }

    public void error(String userId, String credentialsId, String message) {
        log.error("[userId:" + userId + " credentialsId:" + credentialsId + "] " + message);
    }

    public void error(String userId, String credentialsId, String message, Throwable e) {
        log.error("[userId:" + userId + " credentialsId:" + credentialsId + "] " + message, e);
    }

    public void info(String userId, String message) {
        log.info("[userId:" + userId + "] " + message);
    }

    public void info(String userId, String credentialsId, String message) {
        log.info("[userId:" + userId + " credentialsId:" + credentialsId + "] " + message);
    }

    public void info(UUID userId, UUID credentialsId, String message) {
        log.info("[userId:" + UUIDUtils.toTinkUUID(userId) + " credentialsId:" +
                UUIDUtils.toTinkUUID(credentialsId) + "] " + message);
    }

    public void trace(String userId, String message) {
        log.trace("[userId:" + userId + "] " + message);
    }

    public void trace(String userId, String credentialsId, String message) {
        log.trace("[userId:" + userId + " credentialsId:" + credentialsId + "] " + message);
    }

    public void warn(String userId, String message) {
        log.warn("[userId:" + userId + "] " + message);
    }
    
    public void warn(String userId, String message, Throwable e) {
        log.warn("[userId:" + userId + "] " + message, e);
    }

    public void warn(String userId, String credentialsId, String message) {
        log.warn("[userId:" + userId + " credentialsId:" + credentialsId + "] " + message);
    }
    
    public void warn(String userId, String credentialsId, String message, Throwable e) {
        log.warn("[userId:" + userId + " credentialsId:" + credentialsId + "] " + message, e);
    }

}
