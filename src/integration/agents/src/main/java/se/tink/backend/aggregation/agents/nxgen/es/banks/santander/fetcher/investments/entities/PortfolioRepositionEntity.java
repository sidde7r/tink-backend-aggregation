package se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.investments.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.xml.bind.annotation.XmlRootElement;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@XmlRootElement
public class PortfolioRepositionEntity {
    @JsonProperty("codigoEmisionValores")
    private StockEmissionCode emissionCode;

    public StockEmissionCode getEmissionCode() {
        return emissionCode;
    }
}
