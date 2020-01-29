package se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.fetcher.xml;

import javax.xml.bind.annotation.XmlElement;

public class DateEntity {
    @XmlElement(name = "Dt")
    private String date;

    public String getDate() {
        return date;
    }
}
