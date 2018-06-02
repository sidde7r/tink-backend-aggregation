package se.tink.backend.grpc.v1.errors;

import com.google.rpc.LocalizedMessage;
import io.grpc.Metadata;
import io.grpc.Status;
import org.junit.Test;
import se.tink.grpc.v1.models.ErrorCode;
import se.tink.libraries.i18n.LocalizableKey;
import static org.assertj.core.api.Assertions.assertThat;

public class ApiErrorTest {
    @Test
    public void createExceptionWithStatus() throws Exception {
        ApiException exception = new ApiError(Status.Code.UNKNOWN, "test.code", new LocalizableKey("Sure!")).exception();

        assertThat(exception.getStatus().getCode()).isEqualTo(Status.Code.UNKNOWN);
        assertThat(exception.getStatus().getDescription()).isEqualTo("Sure!");
    }

    @Test
    public void createExceptionWithTag() throws Exception {
        ApiException exception = new ApiError(Status.Code.UNKNOWN, "test.code", new LocalizableKey("Sure!")).exception();

        Metadata metadata = exception.getMetadata("en_US");

        assertThat(metadata).isNotNull();

        ErrorCode code = metadata.get(ApiException.ERROR_CODE_KEY);

        assertThat(code).isNotNull();
        assertThat(code.getCode()).isEqualTo("test.code");
    }

    @Test
    public void createExceptionWithLocalizedMessage() throws Exception {
        ApiException exception = new ApiError(Status.Code.UNKNOWN, "test.code", new LocalizableKey("Sure!")).exception();

        Metadata metadata = exception.getMetadata("nl_NL");

        assertThat(metadata).isNotNull();

        LocalizedMessage message = metadata.get(ApiException.LOCALIZED_MESSAGE_KEY);

        assertThat(message).isNotNull();
        assertThat(message.getLocale()).isEqualTo("nl_NL");
        assertThat(message.getMessage()).isEqualTo("Ja, graag");
    }
}
