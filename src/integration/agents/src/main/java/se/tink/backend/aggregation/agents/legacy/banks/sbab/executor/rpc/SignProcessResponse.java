package se.tink.backend.aggregation.agents.banks.sbab.executor.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;
import se.tink.backend.aggregation.agents.banks.sbab.executor.entities.SignatureEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class SignProcessResponse {
    private String id;
    private String status;
    private List<SignatureEntity> signatures;

    @JsonIgnore
    public String getBankIdRef() {
        return signatures.get(0).getId();
    }
}
