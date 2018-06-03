package se.tink.backend.utils;

import java.util.UUID;
import se.tink.libraries.uuid.UUIDUtils;
import se.tink.backend.core.Account;
import se.tink.backend.core.Application;
import se.tink.backend.core.Credentials;
import se.tink.libraries.application.GenericApplication;
import se.tink.backend.core.transfer.Transfer;

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

    public void debug(Credentials credentials, String message) {
        log.debug("[userId:" + credentials.getUserId() + " credentialsId:" + credentials.getId() +
                " providerName:" + credentials.getProviderName() + "] " + message);
    }

    public void error(Credentials credentials, String message) {
        log.error("[userId:" + credentials.getUserId() + " credentialsId:" + credentials.getId() +
                " providerName:" + credentials.getProviderName() + "] " + message);
    }  
    
    public void error(Credentials credentials, String message, Exception e) {
        log.error("[userId:" + credentials.getUserId() + " credentialsId:" + credentials.getId() +
                " providerName:" + credentials.getProviderName() + "] " + message, e);
    }

    public void error(GenericApplication application, Credentials credentials, String message, Throwable e) {
        log.error("[userId:" + UUIDUtils.toTinkUUID(application.getUserId()) +
                " credentialsId:" + credentials.getId() +
                " applicationId:" + UUIDUtils.toTinkUUID(application.getApplicationId()) +
                " providerName:" + credentials.getProviderName() + "] " + message, e);
    }

    public void error(Application application, Credentials credentials, String message) {
        log.error("[userId:" + UUIDUtils.toTinkUUID(application.getUserId()) +
                " credentialsId:" + credentials.getId() +
                " applicationId:" + UUIDUtils.toTinkUUID(application.getId()) +
                " providerName:" + credentials.getProviderName() + "] " + message);
    }

    public void error(Transfer transfer, String message, Throwable e) {
        log.error("[userId:" + UUIDUtils.toTinkUUID(transfer.getUserId()) +
                " credentialsId:" + UUIDUtils.toTinkUUID(transfer.getCredentialsId()) +
                " transferId:" + UUIDUtils.toTinkUUID(transfer.getId()) + "] " + message,
                e);
    }

    public void error(Transfer transfer, String message) {
        log.error("[userId:" + UUIDUtils.toTinkUUID(transfer.getUserId()) +
                " credentialsId:" + UUIDUtils.toTinkUUID(transfer.getCredentialsId()) +
                " transferId:" + UUIDUtils.toTinkUUID(transfer.getId()) + "] " + message);
    }

    public void info(Credentials credentials, String message) {
        log.info("[userId:" + credentials.getUserId() + " credentialsId:" + credentials.getId() +
                " providerName:" + credentials.getProviderName() + "] " + message);
    }

    public void info(String userId, String credentialsId, String message) {
        log.info("[userId:" + userId + " credentialsId:" + credentialsId + "] " + message);
    }

    public void info(Transfer transfer, String message) {
        log.info("[userId:" + UUIDUtils.toTinkUUID(transfer.getUserId()) +
                " credentialsId:" + UUIDUtils.toTinkUUID(transfer.getCredentialsId()) +
                " transferId:" + UUIDUtils.toTinkUUID(transfer.getId()) + "] " + message);
    }

    public void info(Account account, String message) {
        log.info(String.format("[userId:%s credentialsId:%s accountId:%s] %s",
                account.getUserId(), account.getCredentialsId(), account.getId(),
                message));
    }

    public void info(GenericApplication application, Credentials credentials, String message) {
        log.info("[userId:" + UUIDUtils.toTinkUUID(application.getUserId()) +
                " credentialsId:" + credentials.getId() +
                " applicationId:" + UUIDUtils.toTinkUUID(application.getApplicationId()) +
                " providerName:" + credentials.getProviderName() + "] " + message);
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

    public void trace(Credentials credentials, String message) {
        trace(credentials.getUserId(), credentials.getId(), message);
    }

    public void trace(String message, Throwable e) {
        log.trace(message, e);
    }

    public void warn(Credentials credentials, String message) {
        log.warn("[userId:" + credentials.getUserId() + " credentialsId:" + credentials.getId() +
                " providerName:" + credentials.getProviderName() + "] " + message);
    }

    public void warn(Credentials credentials, String message, Throwable e) {
        log.warn("[userId:" + credentials.getUserId() + " credentialsId:" + credentials.getId() +
                " providerName:" + credentials.getProviderName() + "] " + message, e);
    }

    public void warn(Transfer transfer, String message, Throwable e) {
        warn("[userId:" + UUIDUtils.toTinkUUID(transfer.getUserId()) +
                " credentialsId:" + UUIDUtils.toTinkUUID(transfer.getCredentialsId()) +
                " transferId:" + UUIDUtils.toTinkUUID(transfer.getId()) + "] " + message,
                e);
    }

    public void info(String message, Throwable e) {
        log.info(message, e);
    }

    public void debug(GenericApplication application, String message) {
        debug(String.format("[userId:%s applicationId:%s] %s", UUIDUtils.toTinkUUID(application.getUserId()),
                UUIDUtils.toTinkUUID(application.getApplicationId()), message));
    }

    public void debug(Application application, String message) {
        debug(String.format("[userId:%s applicationId:%s] %s", UUIDUtils.toTinkUUID(application.getUserId()),
                UUIDUtils.toTinkUUID(application.getId()), message));
    }

    public void info(Application application, String message) {
        info(String.format("[userId:%s applicationId:%s] %s", UUIDUtils.toTinkUUID(application.getUserId()),
                UUIDUtils.toTinkUUID(application.getId()), message));
    }
}
