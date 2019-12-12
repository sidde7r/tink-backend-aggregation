package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.authenticator.rpc;

import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.authenticator.entities.UserEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LoginResponse {
    private List<String> authenticationData;
    private String authenticationState;
    private UserEntity user;

  public UserEntity getUser() {
    return user;
  }

  public String getAuthenticationState() {
        return authenticationState;
    }
}
