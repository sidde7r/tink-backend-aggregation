package se.tink.backend.grpc.v1.interceptors;

import io.grpc.Context;
import io.grpc.ForwardingServerCallListener;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import io.grpc.Status;
import org.slf4j.event.Level;
import se.tink.backend.auth.AuthenticationContext;
import se.tink.backend.grpc.v1.errors.ApiException;
import se.tink.backend.grpc.v1.utils.TinkGrpcHeaders;
import se.tink.backend.utils.LogUtils;
import se.tink.libraries.validation.validators.LocaleValidator;

public class ExceptionInterceptor implements ServerInterceptor {
    private static final String DEFAULT_LOCALE = "en_US";
    private static final LogUtils log = new LogUtils(ExceptionInterceptor.class);

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> serverCall, Metadata metadata,
            ServerCallHandler<ReqT, RespT> serverCallHandler) {
        ServerCall.Listener<ReqT> listener;

        try {
            listener = serverCallHandler.startCall(serverCall, metadata);
        } catch (Exception ex) {
            // This will handle the exceptions that are thrown by for example other interceptors
            handleException(ex, serverCall, metadata);
            listener = new ServerCall.Listener<ReqT>() {
            };
        }

        // This forwarding listener will handle exceptions that are thrown by service implementations.
        return new ForwardingServerCallListener.SimpleForwardingServerCallListener<ReqT>(listener) {
            @Override
            public void onHalfClose() {
                handleBackendExceptions(serverCall, metadata, super::onHalfClose);
            }

            @Override
            public void onReady() {
                handleBackendExceptions(serverCall, metadata, super::onReady);
            }
        };
    }

    private <ReqT, RespT> void handleBackendExceptions(ServerCall<ReqT, RespT> serverCall, Metadata metadata,
            Runnable runnable) {
        try {
            runnable.run();
        } catch (Exception ex) {
            handleException(ex, serverCall, metadata);
        }
    }

    private <ReqT, RespT> void handleException(Exception ex, ServerCall<ReqT, RespT> call, Metadata metadata) {
        if (ex instanceof ApiException) {
            ApiException e = (ApiException) ex;

            // Only log the cause for Api exceptions since stack traces are cut off in Kibana.
            if (e.getCause() == null) {
                logException(e, e.getSeverity());
            } else {
                logException(e.getCause(), e.getSeverity());
            }

            String locale = getLocale(metadata);

            call.close(e.getStatus(), e.getMetadata(locale));
        } else {
            logException(ex);
            call.close(Status.UNKNOWN, new Metadata());
        }
    }

    /**
     * Log the exception with the default severity as `ERROR`.
     */
    private void logException(Throwable ex) {
        logException(ex, Level.ERROR);
    }

    /**
     * Log the exception, include userId if the user is authenticated.
     */
    private void logException(Throwable ex, Level level) {
        AuthenticationContext authenticationContext = AuthenticationInterceptor.CONTEXT.get(Context.current());

        String message;

        if (authenticationContext != null && authenticationContext.isAuthenticated()) {
            message = "[userId:" + authenticationContext.getUser().getId() + "] " + ex.getMessage();
        } else {
            message = ex.getMessage();
        }

        switch (level) {
        case ERROR:
            log.error(message, ex);
            break;
        case WARN:
            log.warn(message, ex);
            break;
        case INFO:
            log.info(message, ex);
            break;
        case DEBUG:
            log.debug(message, ex);
            break;
        case TRACE:
            log.trace(message, ex);
            break;
        }
    }

    /**
     * Get the locale. Priority is locale of the user, follow by the accept language followed by DEFAULT_LOCALE.
     */
    private String getLocale(Metadata metadata) {
        AuthenticationContext authenticationContext = AuthenticationInterceptor.CONTEXT.get(Context.current());
        if (authenticationContext != null && authenticationContext.isAuthenticated()) {
            return authenticationContext.getUser().getLocale();
        }

        String locale = metadata.get(TinkGrpcHeaders.ACCEPT_LANGUAGE);

        return LocaleValidator.isValid(locale) ? locale : DEFAULT_LOCALE;
    }
}
