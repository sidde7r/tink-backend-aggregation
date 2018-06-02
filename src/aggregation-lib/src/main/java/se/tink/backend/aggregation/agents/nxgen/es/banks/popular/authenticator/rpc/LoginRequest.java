package se.tink.backend.aggregation.agents.nxgen.es.banks.popular.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.es.banks.popular.BancoPopularConstants;

public class LoginRequest {
    private String usuario;
    private String idPassword;
    private String plataforma = BancoPopularConstants.Authentication.PLATAFORMA;

    public String getUsuario() {
        return usuario;
    }

    public LoginRequest setUsuario(String usuario) {
        this.usuario = usuario;
        return this;
    }

    public String getIdPassword() {
        return idPassword;
    }

    public LoginRequest setIdPassword(String idPassword) {
        this.idPassword = idPassword;
        return this;
    }

    public String getPlataforma() {
        return plataforma;
    }

    public void setPlataforma(String plataforma) {
        this.plataforma = plataforma;
    }
}
