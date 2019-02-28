package se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.investments.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.xml.bind.annotation.XmlRootElement;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@XmlRootElement
public class InstrumentDataEntity {
    @JsonProperty("tickerValor")
    private String tickerValue;
    @JsonProperty("nombreValor")
    private String name;
    @JsonProperty("cotizacionValor")
    private QuoteEntity marketPrice;
    @JsonProperty("codigoEmisionValores")
    private StockEmissionCode emissionCode;

    public String getName() {
        return name;
    }
}
