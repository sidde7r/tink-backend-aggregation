package se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.fetcher.investment.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Optional;
import se.tink.backend.aggregation.agents.models.Instrument;
import se.tink.backend.aggregation.agents.models.Instrument.Type;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class FundSelectionsEntity {
    private double share;
    private String insuranceNumber;
    private FundEntity fund;

    @JsonIgnore
    public Instrument toTinkInstrument() {
        Instrument instrument = new Instrument();
        instrument.setUniqueIdentifier(fund.getIsinCode().trim());
        instrument.setName(Optional.ofNullable(fund.getName()).orElse(fund.getFundId()));
        instrument.setIsin(fund.getIsinCode());
        instrument.setType(Type.FUND);
        return instrument;
    }
}
