package se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.fetcher.xml;

import javax.xml.bind.annotation.XmlElement;

public class PartyEntity {

    @XmlElement(name = "Nm")
    private String name;

    public String getName() {
        return name;
    }
}
