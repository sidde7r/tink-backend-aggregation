package se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.fetcher.xml;

import jakarta.xml.bind.annotation.XmlElement;

public class FinInstnIdEntity {
    @XmlElement(name = "BIC")
    private String bic;

    @XmlElement(name = "Nm")
    private String Nm;

    @XmlElement(name = "Othr")
    private OtherEntity other;
}
