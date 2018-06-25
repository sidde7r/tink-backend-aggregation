package se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.authenticator.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LoginRequest {

    private String usuario;
    private String pin;

    public LoginRequest(String usurado, String pin) {
        this.usuario = usurado;
        this.pin = pin;
    }

    public String getPin() {
        return pin;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public void setPin(String pin) {
        this.pin = pin;
    }
}
