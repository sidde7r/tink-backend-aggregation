package se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.xml.bind.annotation.XmlRootElement;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@XmlRootElement
public class CustomerData {
    @JsonProperty("TIPO_DE_PERSONA")
    private String personType;
    @JsonProperty("CODIGO_DE_PERSONA")
    private String personCode;

    public String getPersonType() {
        return personType;
    }

    public String getPersonCode() {
        return personCode;
    }
}
