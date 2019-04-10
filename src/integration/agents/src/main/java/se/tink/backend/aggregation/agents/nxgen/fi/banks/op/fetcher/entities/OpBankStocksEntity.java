package se.tink.backend.aggregation.agents.nxgen.fi.banks.op.fetcher.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.models.Instrument;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class OpBankStocksEntity {
    private OpBankDistributionEntity distributionLine;
    private List<OpBankInstrumentsEntity> instruments;

    @JsonIgnore
    public List<Instrument> getTinkInstruments() {
        if (instruments == null) {
            return Collections.emptyList();
        }

        return instruments.stream()
                .map(instrument -> instrument.toTinkInstrument(Instrument.Type.STOCK))
                .collect(Collectors.toList());
    }
}
