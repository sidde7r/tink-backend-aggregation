package se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.fetcher.xml;

import javax.xml.bind.annotation.XmlElement;

public class SvcrEntity {
    @XmlElement(name = "FinInstnId")
    private FinInstnIdEntity finInstnId;
}
