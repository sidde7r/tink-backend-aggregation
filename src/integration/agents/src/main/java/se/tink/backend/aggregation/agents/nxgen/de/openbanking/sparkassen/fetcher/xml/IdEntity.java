package se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.fetcher.xml;

import javax.xml.bind.annotation.XmlElement;

public class IdEntity {
    @XmlElement(name = "IBAN")
    private String iban;
}
