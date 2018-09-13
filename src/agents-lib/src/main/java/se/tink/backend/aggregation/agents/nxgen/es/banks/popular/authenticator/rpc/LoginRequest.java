package se.tink.backend.aggregation.agents.nxgen.es.banks.popular.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.es.banks.popular.BancoPopularConstants;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LoginRequest {
    private String usuario;
    private String idPassword;
    private String plataforma;

    private LoginRequest(String username, String password) {
        this.usuario = username;
        this.idPassword = password;
        this.plataforma = BancoPopularConstants.Authentication.PLATAFORMA;
    }

    public static LoginRequest build(String username, String password) {
        return new LoginRequest(username, password);
    }
}
