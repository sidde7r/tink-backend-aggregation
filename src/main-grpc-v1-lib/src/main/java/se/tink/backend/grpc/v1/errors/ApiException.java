package se.tink.backend.grpc.v1.errors;

import com.google.rpc.LocalizedMessage;
import io.grpc.Metadata;
import io.grpc.Status;
import io.grpc.protobuf.ProtoUtils;
import java.util.Objects;
import org.slf4j.event.Level;
import se.tink.backend.grpc.v1.utils.TinkGrpcHeaders;
import se.tink.grpc.v1.models.ErrorCode;
import se.tink.libraries.i18n.Catalog;
import se.tink.libraries.i18n.LocalizableKey;

public class ApiException extends RuntimeException {
    public static final Metadata.Key<LocalizedMessage> LOCALIZED_MESSAGE_KEY = ProtoUtils
            .keyForProto(LocalizedMessage.getDefaultInstance());

    public static final Metadata.Key<ErrorCode> ERROR_CODE_KEY = ProtoUtils
            .keyForProto(ErrorCode.getDefaultInstance());

    private final LocalizableKey userMessage;
    private final Status.Code statusCode;
    private final String code;
    private final Level severity;

    ApiException(Status.Code statusCode, String code, LocalizableKey userMessage, Level severity, Throwable cause) {
        super(userMessage != null ? userMessage.get() : null, cause);
        this.statusCode = statusCode;
        this.code = code;
        this.userMessage = userMessage;
        this.severity = severity;
    }

    /**
     * Get the gRPC meta data for this exception.
     */
    public Metadata getMetadata(String locale) {
        Metadata metadata = new Metadata();

        if (code != null) {
            metadata.put(ERROR_CODE_KEY, ErrorCode.newBuilder().setCode(code).build());
        }

        if (userMessage != null) {
            LocalizedMessage message = LocalizedMessage.newBuilder()
                    .setLocale(locale)
                    .setMessage(Catalog.getCatalog(locale).getString(userMessage))
                    .build();

            metadata.put(LOCALIZED_MESSAGE_KEY, message);
        }

        // Temporary hack until the apps have started using the new way of handling errors
        if (Objects.equals(code, "authentication.deprecated_client")) {
            metadata.put(TinkGrpcHeaders.DEPRECATED_CLIENT_KEY, "");
        }

        // Temporary hack until the apps have started using the new way of handling errors
        if (Objects.equals(code, "authentication.unauthorized_device")) {
            metadata.put(TinkGrpcHeaders.DEVICE_UNAUTHORIZED, "");
        }

        return metadata;
    }

    /**
     * Return the gRPC status for this exception. Exception cause is not returned to clients.
     */
    public Status getStatus() {
        return Status.fromCode(statusCode).withDescription(super.getMessage()).withCause(super.getCause());
    }

    /**
     * Get the severity of the error.
     */
    public Level getSeverity() {
        return severity;
    }

}
