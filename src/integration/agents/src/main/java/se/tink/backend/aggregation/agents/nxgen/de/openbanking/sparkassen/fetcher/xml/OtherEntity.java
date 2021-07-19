package se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.fetcher.xml;

import jakarta.xml.bind.annotation.XmlElement;

public class OtherEntity {
    @XmlElement(name = "Id")
    private String id;

    @XmlElement(name = "Issr")
    private String issr;
}
