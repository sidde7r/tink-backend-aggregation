package se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.fetcher.entities.xml;

import javax.xml.bind.annotation.XmlValue;

public class StatusEntity {

    @XmlValue private String status;

    public String getStatus() {
        return status;
    }
}
