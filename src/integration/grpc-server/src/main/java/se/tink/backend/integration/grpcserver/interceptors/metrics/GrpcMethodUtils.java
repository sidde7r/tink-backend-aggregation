package se.tink.backend.integration.gprcserver.interceptors.metrics;

import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Optional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class GrpcMethodUtils {
    private static final Logger logger = LogManager.getLogger(GrpcMethodUtils.class);

    public static <ReqT, RespT> Optional<Method> getCalledMethod(
            ServerCall<ReqT, RespT> serverCall, ServerCallHandler<ReqT, RespT> serverCallHandler) {
        Optional<Object> calledClass = getCalledClass(serverCall, serverCallHandler);
        if (!calledClass.isPresent()) {
            return Optional.empty();
        }
        String methodName = getMethodName(serverCall);
        return Arrays.stream(calledClass.get().getClass().getDeclaredMethods())
                .filter(method -> method.getName().equalsIgnoreCase(methodName))
                .findFirst();
    }

    private static <ReqT, RespT> String getMethodName(ServerCall<ReqT, RespT> serverCall) {
        String[] strings = getFullMethodName(serverCall).split("/");
        if (strings.length != 2) {
            logger.warn("Cannot parse method name for call " + getFullMethodName(serverCall));
            return "";
        }
        return strings[1];
    }

    private static <ReqT, RespT> Optional<Object> getCalledClass(
            ServerCall<ReqT, RespT> serverCall, ServerCallHandler<ReqT, RespT> serverCallHandler) {
        Optional<Object> classValue =
                getMethodHandler(serverCallHandler)
                        .map(methodHandler -> getField(methodHandler, "serviceImpl").orElse(null));
        if (!classValue.isPresent()) {
            logger.warn(
                    "Implemented class was not found for call " + getFullMethodName(serverCall));
        }
        return classValue;
    }

    private static <ReqT, RespT> Optional<Object> getMethodHandler(
            ServerCallHandler<ReqT, RespT> serverCallHandler) {
        String callHandlerName = "callHandler";
        Object callHandler = serverCallHandler;
        Optional<Object> innerCallHandler;
        // If call passed through interceptors, the `serverCallHandler` would have a recursive
        // structure:
        // `interceptor` and `callHandler`. The last `callHandler` would contains `val$method`
        // field.
        do {
            innerCallHandler = getField(callHandler, callHandlerName);
            if (innerCallHandler.isPresent()) {
                callHandler = innerCallHandler.get();
            }
        } while (innerCallHandler.isPresent());
        return getField(callHandler, "method");
    }

    private static Optional<Object> getField(Object object, String fieldName) {
        return Arrays.stream(object.getClass().getDeclaredFields())
                .filter(field -> field.getName().equals(fieldName))
                .peek(field -> field.setAccessible(true))
                .map(
                        field -> {
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
