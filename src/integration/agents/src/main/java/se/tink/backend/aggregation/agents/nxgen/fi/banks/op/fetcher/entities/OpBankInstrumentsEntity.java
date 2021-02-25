package se.tink.backend.aggregation.agents.nxgen.fi.banks.op.fetcher.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import se.tink.backend.aggregation.agents.models.Instrument;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.op.OpBankConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class OpBankInstrumentsEntity {
    private String name;
    private String instrumentId;
    private String tradingCode;
    private String isin;
    private String market;
    private boolean tradable;
    private String type;
    private OpBankWinLoseEntity winLose;
    private OpBankHoldingsEntity holdings;

    @JsonIgnore
    public Instrument toTinkInstrument(Instrument.Type instrumentType) {
        ExactCurrencyAmount marketValue = holdings.getMarketValue().getPriceEur().getTinkAmount();

        Instrument instrument = new Instrument();
        instrument.setUniqueIdentifier(instrumentId);
        instrument.setIsin(isin);
        instrument.setRawType(type);
        instrument.setName(name);
        instrument.setType(instrumentType);
        instrument.setMarketPlace(market);
        instrument.setMarketValue(marketValue.getDoubleValue());
        instrument.setCurrency(marketValue.getCurrencyCode());

        instrument.setQuantity(holdings.getOwnedPcs());
        if (OpBankConstants.Fetcher.INSTRUMENT_TYPE_BOND.equalsIgnoreCase(type)
                && instrument.getQuantity() == 0) {
            instrument.setQuantity(1D);
        }

        if (winLose != null) {
            instrument.setProfit(winLose.getWinLoseAmount().getAmount());
        }

        instrument.setPrice(getInstrumentPrice());

        return instrument;
    }

    /**
     * Returns market price if it's present, if not, calculates market price by dividing marketValue
     * by ownedPcs
     */
    private Double getInstrumentPrice() {
        OpBankPriceEurEntity marketPrice = holdings.getMarketPrice();
        OpBankPriceEurEntity marketValue = holdings.getMarketValue();

        if (marketPrice != null && marketPrice.getPriceEur() != null) {
            return marketPrice.getPriceEur().getAmount();
        } else if (marketValue != null && marketValue.getPriceEur() != null) {
            if (holdings.getOwnedPcs() != 0) {
                return marketValue.getPriceEur().getAmount() / holdings.getOwnedPcs();
            }
        }

        return null;
    }
}
