package se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.investments.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.format.DateTimeFormatter;
import javax.xml.bind.annotation.XmlRootElement;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.entities.InfoEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.investments.entities.InstrumentClauseEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.investments.entities.InstrumentDataEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.investments.entities.QuoteEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@XmlRootElement(name = "methodResult")
public class InstrumentDetailsResponse {
    @JsonIgnore
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private InfoEntity info;

    @JsonProperty("datosBasicos")
    private InstrumentDataEntity basicData;

    @JsonProperty("fechaCotizacion")
    private String quoteDate;

    @JsonProperty("horaCotizacion")
    private String quoteTime;

    @JsonProperty("cotizacionApertura")
    private QuoteEntity openingQuote;

    @JsonProperty("cotizacionCierre")
    private QuoteEntity closingQuote;

    @JsonProperty("valorMaximo")
    private QuoteEntity maxQuote;

    @JsonProperty("valorMinimo")
    private QuoteEntity minQuote;

    @JsonProperty("volumenNegociado")
    private String volume;

    @JsonProperty("clausula")
    private InstrumentClauseEntity clause;

    @JsonIgnore
    public String getName() {
        return basicData.getName();
    }
}
