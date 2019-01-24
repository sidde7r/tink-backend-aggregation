package se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.investments.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.LaCaixaConstants;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.transactionalaccount.entities.BalanceEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.system.rpc.Instrument;

@JsonObject
public class PositionDetailsResponse {
    @JsonProperty("Mercado")
    private String market;
    @JsonProperty("TitulosDepositados")
    private double quantityDeposited;
    @JsonProperty("TitulosDisponibles")
    private double quantityAvailable;
    @JsonProperty("Isin")
    private String isin;
    @JsonProperty("expediente")
    private String contractNumber;
    @JsonProperty("valoracion")
    private BalanceEntity currentValue;
    @JsonProperty("plusvalia")
    private BalanceEntity valueChange;
    @JsonProperty("cotizacion")
    private BalanceEntity price;

    public Instrument toTinkInstrument(String name, Map<String, String> contractToCode) {
        String productCode = contractToCode.getOrDefault(contractNumber, "");
        Instrument.Type type = LaCaixaConstants.INSTRUMENT_TYPE_MAPPER.translate(productCode)
                .orElse(Instrument.Type.OTHER);

        Instrument instrument = new Instrument();

        instrument.setMarketPlace(market);
        instrument.setPrice(price.doubleValue());
        instrument.setMarketValue(currentValue.doubleValue());
        instrument.setName(name);
        instrument.setCurrency(price.getCurrency());
        instrument.setQuantity(quantityAvailable);
        instrument.setType(type);
        instrument.setIsin(isin);
        instrument.setProfit(valueChange.doubleValue());
        instrument.setUniqueIdentifier(market + isin);

        return instrument;
    }

    public String getContractNumber() {
        return contractNumber;
    }
}
