package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.executors.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.executors.entities.ResultsEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Builder
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CompleteTransferResponse {
    private List<ResultsEntity> results;
    private List<CompleteTransferErrorEntity> errors;

    @JsonIgnore
    public boolean hasErrors() {
        return errors != null && !errors.isEmpty();
    }

    public List<ResultsEntity> getResults() {
        return results;
    }
}
