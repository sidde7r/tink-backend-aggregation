package se.tink.backend.aggregation.agents.nxgen.es.openbanking.abanca.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.es.openbanking.abanca.rpc.entity.ErrorsEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ErrorResponse {
    private List<ErrorsEntity> errors;

    @JsonIgnore
    public Optional<ErrorsEntity> getChallengeError() {
        return errors == null
                ? Optional.empty()
                : errors.stream().filter(ErrorsEntity::isChallengeError).findFirst();
    }
}
