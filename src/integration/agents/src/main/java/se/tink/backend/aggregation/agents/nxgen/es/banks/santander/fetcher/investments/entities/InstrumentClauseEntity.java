package se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.investments.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.xml.bind.annotation.XmlRootElement;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@XmlRootElement
public class InstrumentClauseEntity {
    @JsonProperty("linkMifid")
    private String mifidLinkText;
    @JsonProperty("periodoValidez")
    private String validityPeriod;
    @JsonProperty("targetMifid")
    private String mifidTarget;
    @JsonProperty("textoMifid")
    private String mifidText;
    @JsonProperty("tituloMifid")
    private String mifidHeading;
}
