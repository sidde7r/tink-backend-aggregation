package se.tink.backend.aggregation.agents.nxgen.pt.banks.montepio.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.montepio.entities.ErrorEntity;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.montepio.entities.MessageEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public abstract class GenericResponse {
    @JsonProperty("Error")
    protected ErrorEntity error;

    @JsonProperty("Messages")
    private List<MessageEntity> messages;

    public Optional<ErrorEntity> getError() {
        return Optional.ofNullable(error);
    }

    public Optional<List<MessageEntity>> getMessages() {
        return Optional.ofNullable(messages);
    }
}
