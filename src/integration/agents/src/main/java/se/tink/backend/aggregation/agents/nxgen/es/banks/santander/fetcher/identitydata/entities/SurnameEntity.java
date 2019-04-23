package se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.identitydata.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.xml.bind.annotation.XmlRootElement;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@XmlRootElement
public class SurnameEntity {
    @JsonProperty("PARTICULA")
    private String particle;

    @JsonProperty("APELLIDO")
    private String surname;

    public String getParticle() {
        return particle;
    }

    public String getSurname() {
        return surname;
    }
}
