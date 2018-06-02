package se.tink.backend.grpc.v1.interceptors;

import com.google.inject.Inject;
import io.grpc.Context;
import io.grpc.Contexts;
import io.grpc.Grpc;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import java.lang.reflect.Method;
import java.net.SocketAddress;
import java.util.Arrays;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.auth.Authenticated;
import se.tink.backend.grpc.v1.auth.GrpcAuthenticationProvider;
import se.tink.backend.grpc.v1.errors.ApiError;
import se.tink.backend.main.auth.DefaultAuthenticationContext;
import se.tink.backend.main.auth.exceptions.UnauthorizedDeviceException;
import se.tink.backend.main.auth.exceptions.UnsupportedClientException;
import se.tink.backend.utils.RemoteAddressUtils;

public class AuthenticationInterceptor implements ServerInterceptor {
    private static final Logger log = LoggerFactory.getLogger(AuthenticationInterceptor.class);
    public static final Context.Key<DefaultAuthenticationContext> CONTEXT = Context.key("authentication-context");

    private final GrpcAuthenticationProvider authenticationProvider;

    @Inject
    public AuthenticationInterceptor(GrpcAuthenticationProvider authenticationProvider) {
        this.authenticationProvider = authenticationProvider;
    }

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> serverCall, Metadata metadata,
            ServerCallHandler<ReqT, RespT> serverCallHandler) {
        Context context;
        try {
            context = Context.current().withValue(CONTEXT, authenticate(serverCall, metadata, serverCallHandler));
        } catch (UnsupportedClientException e) {
            throw ApiError.Authentication.DEPRECATED_CLIENT.withCause(e).exception();
        } catch (UnauthorizedDeviceException e) {
            throw ApiError.Authentication.UNAUTHORIZED_DEVICE.withCause(e).exception();
        } catch (Exception e) {
            throw ApiError.Authentication.UNAUTHENTICATED.withWarnSeverity().withCause(e).exception();
        }

        return Contexts.interceptCall(context, serverCall, metadata, serverCallHandler);
    }

    private <ReqT, RespT> DefaultAuthenticationContext authenticate(ServerCall<ReqT, RespT> serverCall,
            Metadata metadata,
            ServerCallHandler<ReqT, RespT> serverCallHandler) throws IllegalAccessException {

        Optional<Method> calledMethod = getCalledMethod(serverCall, serverCallHandler);
        Optional<Authenticated> authenticated = calledMethod.map(method -> method.getAnnotation(Authenticated.class));
        if (!authenticated.isPresent()) {
            String message = "Cannot find @Authenticated annotation for call " + getFullMethodName(serverCall);
            throw ApiError.Authentication.INTERNAL_ERROR.withCause(new Exception(message)).exception();
        }
        SocketAddress socketAddress = serverCall.getAttributes().get(Grpc.TRANSPORT_ATTR_REMOTE_ADDR);
        return authenticationProvider.authenticate(metadata, authenticated.get(), RemoteAddressUtils.getRemoteAddress(socketAddress));
    }


    private <ReqT, RespT> Optional<Method> getCalledMethod(ServerCall<ReqT, RespT> serverCall,
            ServerCallHandler<ReqT, RespT> serverCallHandler) {
        Optional<Object> calledClass = getCalledClass(serverCall, serverCallHandler);
        if (!calledClass.isPresent()) {
            return Optional.empty();
        }
        String methodName = getMethodName(serverCall);
        return Arrays.stream(calledClass.get().getClass().getDeclaredMethods())
                .filter(method -> method.getName().equalsIgnoreCase(methodName))
                .findFirst();
    }

    private <ReqT, RespT> String getMethodName(ServerCall<ReqT, RespT> serverCall) {
        String[] strings = getFullMethodName(serverCall).split("/");
        if (strings.length != 2) {
            log.warn("Cannot parse method name for call " + getFullMethodName(serverCall));
            return "";
        }
        return strings[1];
    }

    private <ReqT, RespT> Optional<Object> getCalledClass(ServerCall<ReqT, RespT> serverCall,
            ServerCallHandler<ReqT, RespT> serverCallHandler) {
        Optional<Object> classValue = getMethodHandler(serverCallHandler)
                .map(methodHandler -> getField(methodHandler, "serviceImpl").orElse(null));
        if (!classValue.isPresent()) {
            log.warn("Implemented class was not found for call " + getFullMethodName(serverCall));
        }
        return classValue;
    }

    private <ReqT, RespT> Optional<Object> getMethodHandler(ServerCallHandler<ReqT, RespT> serverCallHandler) {
        String callHandlerName = "callHandler";
        Object callHandler = serverCallHandler;
        Optional<Object> innerCallHandler;
        // If call passed through interceptors, the `serverCallHandler` would have a recursive structure:
        // `interceptor` and `callHandler`. The last `callHandler` would contains `val$method` field.
        do {
            innerCallHandler = getField(callHandler, callHandlerName);
            if (innerCallHandler.isPresent()) {
                callHandler = innerCallHandler.get();
            }
        } while (innerCallHandler.isPresent());
        return getField(callHandler, "method");
    }

    private Optional<Object> getField(Object object, String fieldName) {
        return Arrays.stream(object.getClass().getDeclaredFields())
                .filter(field -> field.getName().equals(fieldName))
                .peek(field -> field.setAccessible(true))
                .map(field -> {
                    try {
                        return field.get(object);
                    } catch (IllegalAccessException e) {
                        return null;
                    }
                })
                .findFirst();
    }

    private static String getFullMethodName(ServerCall serverCall) {
        return serverCall.getMethodDescriptor().getFullMethodName();
    }
}
