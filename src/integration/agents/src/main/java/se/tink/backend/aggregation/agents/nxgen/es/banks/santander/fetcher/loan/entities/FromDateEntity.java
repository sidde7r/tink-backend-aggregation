package se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.loan.entities;

import jakarta.xml.bind.annotation.XmlRootElement;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@XmlRootElement(name = "fechaDesde")
public class FromDateEntity {
    private DateEntity date;

    public void setDate(DateEntity date) {
        this.date = date;
    }
}
