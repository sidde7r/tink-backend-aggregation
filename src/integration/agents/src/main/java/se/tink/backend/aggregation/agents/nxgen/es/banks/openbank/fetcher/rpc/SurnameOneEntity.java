package se.tink.backend.aggregation.agents.nxgen.es.banks.openbank.fetcher.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@SuppressWarnings("FieldCanBeLocal")
@JsonObject
public class SurnameOneEntity {
    @JsonProperty("apellido")
    private String surname;

    @JsonProperty("particula")
    private String title;

    public void setSurname(String surname) {
        this.surname = surname != null ? surname.trim() : null;
    }

    public String getSurname() {
        return surname;
    }
}
