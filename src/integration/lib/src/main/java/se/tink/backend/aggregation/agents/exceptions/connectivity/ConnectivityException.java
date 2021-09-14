package se.tink.backend.aggregation.agents.exceptions.connectivity;

import lombok.Getter;
import lombok.NonNull;
import se.tink.connectivity.errors.ConnectivityError;
import se.tink.libraries.i18n.LocalizableKey;

public final class ConnectivityException extends RuntimeException {

    private final LocalizableKey userMessage;
    @Getter private final ConnectivityError error;
    private final Throwable cause;
    private final String internalMessage;

    ConnectivityException(ConnectivityError error) {
        this(error, new LocalizableKey(error.getDisplayMessage()), null, null);
    }

    private ConnectivityException(
            @NonNull ConnectivityError error,
            LocalizableKey userMessage,
            Throwable cause,
            String internalMessage) {
        super(getMessage(error, internalMessage), cause);
        this.error = error;
        this.userMessage = userMessage;
        this.cause = cause;
        this.internalMessage = internalMessage;
    }

    private static String getMessage(@NonNull ConnectivityError error, String internalMessage) {
        return internalMessage != null
                ? internalMessage
                : String.format("Cause: %s.%s", error.getType(), error.getDetails().getReason());
    }

    public ConnectivityException withUserMessage(LocalizableKey userMessage) {
        return new ConnectivityException(this.error, userMessage, this.cause, this.internalMessage);
    }

    public ConnectivityException withCause(Throwable cause) {
        return new ConnectivityException(this.error, this.userMessage, cause, this.internalMessage);
    }

    public ConnectivityException withExceptionMessage(String internalMessage) {
        return new ConnectivityException(this.error, this.userMessage, this.cause, internalMessage);
    }
}
