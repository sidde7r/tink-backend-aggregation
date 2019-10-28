package se.tink.backend.aggregation.agents.nxgen.pt.banks.montepio.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.montepio.MontepioConstants;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.montepio.entities.ErrorEntity;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.montepio.entities.MessageEntity;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.montepio.entities.ResultEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class GenericResponse {
    @JsonProperty("Error")
    private ErrorEntity error;

    @JsonProperty("Messages")
    private List<MessageEntity> messages;

    @JsonProperty("Result")
    private ResultEntity resultEntity;

    public Optional<ErrorEntity> getError() {
        return Optional.ofNullable(error);
    }

    public Optional<List<MessageEntity>> getMessages() {
        return Optional.ofNullable(messages);
    }

    public ResultEntity getResultEntity() {
        return resultEntity;
    }

    public boolean isWrongCredentials() {
        return error != null
                && (MontepioConstants.ErrorMessages.INVALID_LOGIN.equals(error.getCode())
                        || MontepioConstants.ErrorMessages.INVALID_PASSWORD.equals(
                                error.getCode()));
    }
}
