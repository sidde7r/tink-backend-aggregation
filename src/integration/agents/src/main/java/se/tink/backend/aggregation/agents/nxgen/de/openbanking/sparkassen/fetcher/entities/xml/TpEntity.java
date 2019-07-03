package se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.fetcher.entities.xml;

import javax.xml.bind.annotation.XmlElement;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TpEntity {
    @XmlElement(name = "CdOrPrtry")
    private CdOrPrtryEntity cdOrPrtry;
}
