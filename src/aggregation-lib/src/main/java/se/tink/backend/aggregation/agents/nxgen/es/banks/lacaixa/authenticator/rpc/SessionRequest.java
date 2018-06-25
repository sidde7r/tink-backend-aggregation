package se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.authenticator.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class SessionRequest {
    private String idioma;
    private String origen;
    private String canal;
    private String idInstalacion;

    public SessionRequest(String idioma, String origen, String canal, String idInstalacion) {
        this.idioma = idioma;
        this.origen = origen;
        this.canal = canal;
        this.idInstalacion = idInstalacion;
    }

    public SessionRequest setIdioma(String idioma) {
        this.idioma = idioma;
        return this;
    }

    public SessionRequest setOrigen(String origen) {
        this.origen = origen;
        return this;
    }

    public SessionRequest setCanal(String canal) {
        this.canal = canal;
        return this;
    }

    public SessionRequest setIdInstalacion(String idInstalacion) {
        this.idInstalacion = idInstalacion;
        return this;
    }
}
