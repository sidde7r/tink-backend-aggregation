package se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.fetcher.entities.xml;

import javax.xml.bind.annotation.XmlElement;
import se.tink.backend.aggregation.annotations.JsonObject;

// XML: Dt
@JsonObject
public class DateEntity {
    @XmlElement(name = "Dt")
    private String date;

    public String getDate() {
        return date;
    }
}
