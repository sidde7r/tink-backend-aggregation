package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.investment.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.BecConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.agents.models.Instrument;

@JsonObject
public class PortfolioEntity {
    @JsonProperty("paperType")
    private String instrumentsType;
    private String dataType;
    private double typeAmount;
    private String typeAmountTxt;
    @JsonProperty("papers")
    private List<InstrumentEntity> instruments;

    public String getInstrumentsType() {
        return instrumentsType;
    }

    public String getDataType() {
        return dataType;
    }

    public double getTypeAmount() {
        return typeAmount;
    }

    public String getTypeAmountTxt() {
        return typeAmountTxt;
    }

    public List<InstrumentEntity> getInstruments() {
        return instruments;
    }


    public boolean isInstrumentTypeKnown() {
        return BecConstants.INSTRUMENT_TYPES.containsKey(dataType);
    }

    public Instrument.Type toTinkType() {
        return BecConstants.INSTRUMENT_TYPES.getOrDefault(dataType, Instrument.Type.OTHER);
    }

}
