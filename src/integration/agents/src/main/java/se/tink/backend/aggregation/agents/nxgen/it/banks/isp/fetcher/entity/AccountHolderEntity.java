package se.tink.backend.aggregation.agents.nxgen.it.banks.isp.fetcher.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class AccountHolderEntity {

    @JsonProperty("cognome")
    private String surname;

    private String id;

    @JsonProperty("nome")
    private String firstName;

    @JsonProperty("denominazione")
    private String fullName;

    private String nsg;
    private int progressivoOrdinante;
    private String tipoIntestatario;
}
