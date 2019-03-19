package se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.investments.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.xml.bind.annotation.XmlRootElement;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@XmlRootElement
public class StockEmissionCode {
    @JsonProperty("CODIGO_DE_VALOR")
    private String stockCode;

    @JsonProperty("CODIGO_DE_EMISION")
    private String emissionCode;

    public String getStockCode() {
        return stockCode;
    }

    public String getEmissionCode() {
        return emissionCode;
    }

    @JsonIgnore
    public String getCombinedCode() {
        return String.format("%s%s", emissionCode, stockCode);
    }
}
