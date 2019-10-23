package se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.fetcher.entities.xml;

import javax.xml.bind.annotation.XmlElement;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
// XML: Othr
public class OtherEntity {
    @XmlElement(name = "Id")
    private String id;

    @XmlElement(name = "Issr")
    private String issr;
}
