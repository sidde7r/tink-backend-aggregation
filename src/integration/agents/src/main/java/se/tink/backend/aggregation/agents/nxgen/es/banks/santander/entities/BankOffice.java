package se.tink.backend.aggregation.agents.nxgen.es.banks.santander.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.xml.bind.annotation.XmlRootElement;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@XmlRootElement
public class BankOffice {
    @JsonProperty("EMPRESA")
    private String company;

    @JsonProperty("CENTRO")
    private String office;

    public String getCompany() {
        return company;
    }

    public String getOffice() {
        return office;
    }
}
