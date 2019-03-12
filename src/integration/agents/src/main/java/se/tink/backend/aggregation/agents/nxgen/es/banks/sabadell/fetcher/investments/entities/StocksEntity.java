package se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.investments.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import se.tink.backend.aggregation.agents.AgentParsingUtils;
import se.tink.backend.aggregation.agents.models.Instrument;
import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.SabadellConstants;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class StocksEntity {
    private String date;
    private String time;
    private String description;
    private String code;
    private AmountEntity lastPrice;
    private String percent;
    private String percentPlusvalua;
    private String isin;
    private AmountEntity buyPrice;
    private AmountEntity surplusValua;
    private String nStocks;
    private AmountEntity amount;
    private String marketCode;
    private String tick;
    private String currency;

    @JsonIgnore
    public Instrument toTinkInstrument() {
        Instrument instrument = new Instrument();

        instrument.setAverageAcquisitionPrice(AgentParsingUtils.parseAmount(buyPrice.getValue()));
        instrument.setCurrency(currency);
        instrument.setIsin(isin);
        instrument.setMarketPlace(code);
        instrument.setMarketValue(AgentParsingUtils.parseAmount(amount.getValue()));
        instrument.setName(description);
        instrument.setPrice(AgentParsingUtils.parseAmount(lastPrice.getValue()));
        instrument.setQuantity(AgentParsingUtils.parseAmount(nStocks));
        instrument.setType(Instrument.Type.STOCK);
        instrument.setUniqueIdentifier(isin + marketCode);
        instrument.setProfit(
                surplusValua
                                .getValue()
                                .equals(SabadellConstants.Constants.NOT_AVAILABLE_ABBREVIATION)
                        ? 0.0
                        : AgentParsingUtils.parseAmount(surplusValua.getValue()));

        return instrument;
    }
}
