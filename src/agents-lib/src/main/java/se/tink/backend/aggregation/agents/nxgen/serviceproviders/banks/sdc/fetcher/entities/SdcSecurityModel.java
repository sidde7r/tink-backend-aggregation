package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.fetcher.entities;

import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.SdcConstants;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.core.Amount;
import se.tink.backend.aggregation.agents.models.Instrument;

@JsonObject
public class SdcSecurityModel {
    private static final AggregationLogger LOGGER = new AggregationLogger(SdcSecurityModel.class);

    private String countryCode;
    private int delay;
    private String id;
    private SdcChange intradayChange;//Can this field somehow be used to calculate the price and profit?
    private String isin;
    private String name;
    private String sdcIdentifier;
    private String securityType;
    private String source;
    private long timeQuoteDataLoaded;
    private SdcAmount value;

    public Instrument toInstrument(SdcQuantity quantity) {
        Instrument instrument = new Instrument();
        instrument.setQuantity(quantity.getQuantity());

        String isin = this.isin;
        String market = countryCode;
        instrument.setIsin(isin);
        instrument.setMarketPlace(market);
        instrument.setUniqueIdentifier(isin + market);

        Optional<SdcAmount> value = getValue();
        instrument.setCurrency(value.map(SdcAmount::getCurrency).orElse(null));
        instrument.setMarketValue(value
                .map(SdcAmount::toTinkAmount)
                .map(Amount::getValue)
                .orElse(0d));
        instrument.setName(name);
        instrument.setRawType(securityType);
        instrument.setType(parseInstrumentType());
        return instrument;
    }

    private Instrument.Type parseInstrumentType() {
        return SdcConstants.Fetcher.Investment.InstrumentTypes.parse(securityType)
                .orElse(Instrument.Type.OTHER);
    }

    private Optional<SdcAmount> getValue() {
        if (value == null) {
            LOGGER.debug(String.format("%s - no value found for this instrument. Is this a bad thing?",
                    SdcConstants.Fetcher.Investment.INSTRUMENTS));
            return Optional.empty();
        }
        return Optional.of(value);
    }
}
