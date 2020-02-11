package se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.fetcher.xml;

import javax.xml.bind.annotation.XmlElement;

public class RemittanceInformationEntity {

    @XmlElement(name = "Ustrd")
    private String unstructured;

    public String getUnstructured() {
        return unstructured;
    }
}
