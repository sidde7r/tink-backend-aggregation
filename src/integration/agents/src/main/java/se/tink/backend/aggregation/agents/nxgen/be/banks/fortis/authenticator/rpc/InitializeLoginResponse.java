package se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.rpc;

import lombok.Getter;
import lombok.Setter;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.entities.InitiateLoginValueEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@Getter
@Setter
@JsonObject
public class InitializeLoginResponse extends BusinessMessageResponse<InitiateLoginValueEntity> {

    private String loginSessionId;
}
