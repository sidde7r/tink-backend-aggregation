package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.fetcher.entities;

import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.agents.models.Instrument;

/*
{
"label" : "Skagen Kon-Tiki",
"marketValue" : {
    "localizedValue" : "10.00",
    "localizedValueWithCurrency" : "10.00 SEK",
    "value" : 1000,
    "scale" : 2,
    "currency" : "SEK",
    "localizedValueWithCurrencyAtEnd" : "10.00 SEK",
    "roundedAmountWithIsoCurrency" : "SEK10",
    "roundedAmountWithCurrencySymbol" : "10 SEK"
},
"depositDetailsModel" : SdcDepositDetails
}
*/
@JsonObject
public class SdcDepositContent {
    private String label;
    private SdcAmount marketValue;
    private SdcDepositDetails depositDetailsModel;

    public Instrument toInstrument() {
        Instrument instrument = new Instrument();

        instrument.setName(label);
        instrument.setMarketValue(marketValue.toTinkAmount().getValue());
        instrument.setCurrency(marketValue.getCurrency());

        return instrument;
    }

    public String getLabe() {
        return label;
    }

    public SdcAmount getMarketValue() {
        return marketValue;
    }

    public SdcDepositDetails getDepositDetailsModel() {
        return depositDetailsModel;
    }
}
