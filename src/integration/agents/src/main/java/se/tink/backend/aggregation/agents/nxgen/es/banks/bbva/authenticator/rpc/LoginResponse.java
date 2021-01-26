package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.authenticator.rpc;

import java.util.List;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.authenticator.entities.UserEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class LoginResponse {
    private List<String> authenticationData;
    private String authenticationState;
    private String multistepProcessId;
    private UserEntity user;
}
