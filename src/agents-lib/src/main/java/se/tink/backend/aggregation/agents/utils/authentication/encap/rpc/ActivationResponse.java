package se.tink.backend.aggregation.agents.utils.authentication.encap.rpc;

import se.tink.backend.aggregation.agents.utils.authentication.encap.entities.ActivationResultEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ActivationResponse extends AbstractResponse {
    private ActivationResultEntity result;

    public ActivationResultEntity getResult() {
        return result;
    }
}
