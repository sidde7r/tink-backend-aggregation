package se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.investments.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.xml.bind.annotation.XmlRootElement;
import se.tink.backend.aggregation.agents.models.Instrument;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.SantanderEsApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.entities.AmountEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.investments.rpc.InstrumentDetailsResponse;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@XmlRootElement
public class PortfolioContentEntity {
    @JsonProperty("codigoEmisionValores")
    private StockEmissionCode emissionCode;

    @JsonProperty("tickerValor")
    private String tickerValue;

    @JsonProperty("posicionValor")
    private AmountEntity marketValue;

    @JsonProperty("fechaCotizacion")
    private String quoteDate;

    @JsonProperty("horaCotizacion")
    private String quoteTime;

    @JsonProperty("cotizacionValor")
    private QuoteEntity marketPrice;

    @JsonProperty("numTitulos")
    private int quantity;

    public Instrument toInstrument(SantanderEsApiClient apiClient, String userDataXml) {
        InstrumentDetailsResponse instrumentDetails =
                apiClient.fetchInstrumentDetails(userDataXml, emissionCode);

        Instrument instrument = new Instrument();
        instrument.setUniqueIdentifier(emissionCode.getCombinedCode());
        instrument.setMarketValue(marketValue.getAmountAsDouble());
        instrument.setName(instrumentDetails.getName());
        instrument.setQuantity((double) quantity);
        instrument.setType(Instrument.Type.STOCK);
        instrument.setCurrency(marketPrice.getCurrency());
        instrument.setPrice(marketPrice.getTinkAmount().getDoubleValue());
        instrument.setTicker(tickerValue);

        return instrument;
    }
}
