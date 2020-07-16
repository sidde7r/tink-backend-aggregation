package se.tink.backend.aggregation.workers.encryption.models;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.function.Consumer;

public class VersionDeserializer {
    private static final ObjectMapper mapper =
            new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private Consumer<EncryptedCredentials> handlerDefault;
    private Consumer<EncryptedCredentialsV1> handlerV1;
    // Add new versions here

    public VersionDeserializer addVersion1Handler(Consumer<EncryptedCredentialsV1> handler) {
        handlerV1 = handler;
        return this;
    }

    public VersionDeserializer addDefaultHandler(Consumer<EncryptedCredentials> handler) {
        handlerDefault = handler;
        return this;
    }

    public void handle(String input) {
        if (handlerDefault == null) {
            throw new IllegalStateException("Deserialization started without default handler.");
        }
        if (input == null) {
            throw new NullPointerException();
        }

        try {
            EncryptedCredentials head = mapper.readValue(input, EncryptedCredentials.class);

            switch (head.getVersion()) {
                case EncryptedCredentialsV1.VERSION:
                    if (handlerV1 == null) {
                        handleDefault(head);
                        break;
                    }
                    EncryptedCredentialsV1 deserialized =
                            mapper.readValue(input, EncryptedCredentialsV1.class);
                    handlerV1.accept(deserialized);
                    break;
                case 2:
                    // This shows where to add a new version, for now though, just waterfall down to
                    // default
                default:
                    handleDefault(head);
                    break;
            }

        } catch (IOException e) {
            // if serialized data is not deserializable
            throw new IllegalStateException(e);
        }
    }

    private void handleDefault(EncryptedCredentials head) {
        handlerDefault.accept(head);
    }
}
