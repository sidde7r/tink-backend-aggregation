package se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.authenticator.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class SessionResponse {
    private String iteraciones;
    private String semilla;
    private String operacion;
    private String constante;

    public String getIteraciones() {
        return iteraciones;
    }

    public String getSemilla() {
        return semilla;
    }

    public String getOperacion() {
        return operacion;
    }

    public String getConstante() {
        return constante;
    }

    public void setIteraciones(String interactiones) {
        this.iteraciones = interactiones;
    }

    public void setSemilla(String semilla) {
        this.semilla = semilla;
    }

    public void setOperacion(String operacion) {
        this.operacion = operacion;
    }

    public void setConstante(String constante) {
        this.constante = constante;
    }
}
