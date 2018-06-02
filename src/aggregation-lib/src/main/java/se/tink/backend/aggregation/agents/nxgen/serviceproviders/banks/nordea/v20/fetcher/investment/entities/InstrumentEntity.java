package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v20.fetcher.investment.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.serializer.NordeaHashMapDeserializer;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v20.NordeaV20Constants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.system.rpc.Instrument;
import se.tink.backend.utils.StringUtils;

@JsonObject
public class InstrumentEntity {
    private InstrumentIdEntity instrumentId;
    @JsonDeserialize(using = NordeaHashMapDeserializer.class)
    private String instrumentName;
    @JsonDeserialize(using = NordeaHashMapDeserializer.class)
    private String instrumentType;
    @JsonDeserialize(using = NordeaHashMapDeserializer.class)
    private String price;
    @JsonDeserialize(using = NordeaHashMapDeserializer.class)
    private String priceTime;
    @JsonDeserialize(using = NordeaHashMapDeserializer.class)
    private String todaysChange;
    @JsonProperty("todaysChangePct")
    @JsonDeserialize(using = NordeaHashMapDeserializer.class)
    private String todaysChangePercentage;

    public InstrumentIdEntity getInstrumentId() {
        return instrumentId;
    }

    public String getInstrumentName() {
        return instrumentName;
    }

    public String getInstrumentType() {
        return instrumentType;
    }

    public Double getPrice() {
        return price == null || price.isEmpty() ? null : StringUtils.parseAmount(price);
    }

    public String getPriceTime() {
        return priceTime;
    }

    public String getTodaysChange() {
        return todaysChange;
    }

    public String getTodaysChangePercentage() {
        return todaysChangePercentage;
    }

    public Instrument.Type getTinkInstrumentType() {
        switch (getInstrumentType().toLowerCase()) {
        case NordeaV20Constants.Investments.InstrumentTypes.EQUITY:
            return Instrument.Type.STOCK;
        case NordeaV20Constants.Investments.InstrumentTypes.FUND:
            return Instrument.Type.FUND;
        case NordeaV20Constants.Investments.InstrumentTypes.DERIVATIVE:
            // Intentional fall through
        default:
            return Instrument.Type.OTHER;
        }
    }
}
