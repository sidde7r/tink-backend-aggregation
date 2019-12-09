package se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.fetcher.entities.xml;

import javax.xml.bind.annotation.XmlElement;
import se.tink.backend.aggregation.annotations.JsonObject;

// XML: Id
@JsonObject
public class IdEntity {
    @XmlElement(name = "IBAN")
    private String iban;
}
