package se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.authenticator.entity.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ClientEntity {
    @JsonProperty("IDPN")
    private String idpn;

    @JsonProperty("Nome")
    private String name;

    @JsonProperty("NomeAbrev")
    private String nameAbbrev;
}
