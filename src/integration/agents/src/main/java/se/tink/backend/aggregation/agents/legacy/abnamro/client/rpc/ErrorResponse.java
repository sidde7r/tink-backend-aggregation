package se.tink.backend.aggregation.agents.abnamro.client.rpc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import se.tink.libraries.serialization.utils.SerializationUtils;
import se.tink.backend.aggregation.agents.abnamro.client.model.ErrorEntity;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ErrorResponse {
    private List<ErrorEntity> messages;

    public List<ErrorEntity> getMessages() {
        return messages;
    }

    public void setMessages(List<ErrorEntity> messages) {
        this.messages = messages;
    }

    public Optional<ErrorEntity> getErrorByKey(String errorKey) {
        if (messages == null) {
            return Optional.empty();
        }

        for (ErrorEntity message : messages) {
            if (Objects.equals(errorKey, message.getMessageKey())) {
                return Optional.of(message);
            }
        }

        return Optional.empty();
    }

    /**
     * Message text of a message entity is quite messy so serialize the whole thing to json
     */
    public String getErrorDetails() {
        return messages == null ? null : SerializationUtils.serializeToString(messages);
    }
}
