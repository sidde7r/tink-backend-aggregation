package se.tink.backend.insights.utils;

import java.util.UUID;
import se.tink.backend.insights.core.valueobjects.UserId;
import se.tink.libraries.uuid.UUIDUtils;

/**
 * Tink implementation of logging. Must be thread-safe.
 */
@SuppressWarnings("rawtypes")
public class LogUtils extends se.tink.libraries.log.LogUtils {
    public LogUtils(Class clazz) {
        super(clazz);
    }

    public void debug(UserId userId, String message) {
        log.debug("[userId:" + userId.value() + "] " + message);
    }

    public void debug(UserId userId, String credentialsId, String message) {
        log.debug("[userId:" + userId.value() + " credentialsId:" + credentialsId + "] " + message);
    }

    public void error(UserId userId, String message) {
        log.error("[userId:" + userId.value() + "] " + message);
    }

    public void error(UserId userId, String message, Throwable e) {
        log.error("[userId:" + userId.value() + "] " + message, e);
    }

    public void error(UserId userId, String credentialsId, String message) {
        log.error("[userId:" + userId.value() + " credentialsId:" + credentialsId + "] " + message);
    }

    public void error(UserId userId, String credentialsId, String message, Throwable e) {
        log.error("[userId:" + userId.value() + " credentialsId:" + credentialsId + "] " + message, e);
    }

    public void info(UserId userId, String message) {
        log.info("[userId:" + userId.value() + "] " + message);
    }

    public void info(UserId userId, String credentialsId, String message) {
        log.info("[userId:" + userId.value() + " credentialsId:" + credentialsId + "] " + message);
    }

    public void info(UUID userId, UUID credentialsId, String message) {
        log.info("[userId:" + UUIDUtils.toTinkUUID(userId) + " credentialsId:" +
                UUIDUtils.toTinkUUID(credentialsId) + "] " + message);
    }

    public void trace(UserId userId, String message) {
        log.trace("[userId:" + userId.value() + "] " + message);
    }

    public void trace(UserId userId, String credentialsId, String message) {
        log.trace("[userId:" + userId.value() + " credentialsId:" + credentialsId + "] " + message);
    }

    public void warn(UserId userId, String message) {
        log.warn("[userId:" + userId.value() + "] " + message);
    }

    public void warn(UserId userId, String message, Throwable e) {
        log.warn("[userId:" + userId.value() + "] " + message, e);
    }

    public void warn(UserId userId, String credentialsId, String message) {
        log.warn("[userId:" + userId.value() + " credentialsId:" + credentialsId + "] " + message);
    }

    public void warn(UserId userId, String credentialsId, String message, Throwable e) {
        log.warn("[userId:" + userId.value() + " credentialsId:" + credentialsId + "] " + message, e);
    }

}
