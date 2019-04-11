package se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class SessionRequest {

    @JsonProperty("idioma")
    private String language;

    @JsonProperty("origen")
    private String origin;

    @JsonProperty("canal")
    private String channel;

    @JsonProperty("idInstalacion")
    private String installationId;

    @JsonProperty("tecladoVirtual")
    private String virtualKeyboard;

    public SessionRequest(
            String idioma,
            String origen,
            String canal,
            String idInstalacion,
            String virtualKeyboard) {
        this.language = idioma;
        this.origin = origen;
        this.channel = canal;
        this.installationId = idInstalacion;
        this.virtualKeyboard = virtualKeyboard;
    }
}
