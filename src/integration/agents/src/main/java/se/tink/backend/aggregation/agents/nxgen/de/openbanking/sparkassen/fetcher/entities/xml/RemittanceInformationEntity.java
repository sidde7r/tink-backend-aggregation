package se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.fetcher.entities.xml;

import javax.xml.bind.annotation.XmlElement;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class RemittanceInformationEntity {

    @XmlElement(name = "Ustrd")
    private String unstructured;

    public String getUnstructured() {
        return unstructured;
    }
}
