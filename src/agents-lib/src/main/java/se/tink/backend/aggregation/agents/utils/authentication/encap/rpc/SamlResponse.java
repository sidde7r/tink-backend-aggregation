package se.tink.backend.aggregation.agents.utils.authentication.encap.rpc;

import se.tink.backend.aggregation.agents.utils.authentication.encap.entities.SamlResultEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class SamlResponse extends AbstractResponse {
    private SamlResultEntity result;

    public SamlResultEntity getResult() {
        return result;
    }
}
