package se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.xml.bind.annotation.XmlRootElement;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@XmlRootElement
public class Office {
    @JsonProperty("ENTIDAD")
    private String branch;
    @JsonProperty("OFICINA")
    private String office;

    public String getBranch() {
        return branch;
    }

    public String getOffice() {
        return office;
    }
}
