package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.executor.payment.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.executor.payment.entity.ErrorDetail;
import se.tink.backend.aggregation.annotations.JsonObject;

@Getter
@JsonObject
public class ErrorResponse {

    private List<ErrorDetail> errors;
    private boolean success;

    @JsonIgnore
    public boolean errorContains(String error) {
        return errors.stream().anyMatch(errorDetail -> errorDetail.getMessage().contains(error));
    }
}
