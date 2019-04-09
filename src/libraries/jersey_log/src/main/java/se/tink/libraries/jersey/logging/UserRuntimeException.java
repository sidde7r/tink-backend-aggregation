package se.tink.libraries.jersey.logging;

/** An "helper exception" useful to decorate an exception with a userId. */
public class UserRuntimeException extends RuntimeException {
    public UserRuntimeException(String userId, Throwable e) {
        super(String.format("[userId:%s] Something went wrong.", userId), e);
    }

    public UserRuntimeException(String userId, String message, Throwable e) {
        super(String.format("[userId:%s] %s", userId, message), e);
    }
}
