package se.tink.backend.aggregation.agents.nxgen.be.banks.axa.authenticator.entities;

import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AuthDataEntity {

    private String assertionsComplete;
    private String challenge;
    private List<ControlFlowEntity> controlFlow;
    private String data;
    private ResponseDataEntity state;
}
