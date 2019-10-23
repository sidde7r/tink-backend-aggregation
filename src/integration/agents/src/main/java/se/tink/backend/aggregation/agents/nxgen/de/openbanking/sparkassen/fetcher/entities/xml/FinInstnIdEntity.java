package se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.fetcher.entities.xml;

import javax.xml.bind.annotation.XmlElement;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class FinInstnIdEntity {
    @XmlElement(name = "BIC")
    private String bic;

    @XmlElement(name = "Nm")
    private String Nm;

    @XmlElement(name = "Othr")
    private OtherEntity other;
}
