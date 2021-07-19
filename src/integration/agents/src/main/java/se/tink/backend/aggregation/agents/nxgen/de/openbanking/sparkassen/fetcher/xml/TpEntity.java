package se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.fetcher.xml;

import jakarta.xml.bind.annotation.XmlElement;

public class TpEntity {
    @XmlElement(name = "CdOrPrtry")
    private CdOrPrtryEntity cdOrPrtry;
}
