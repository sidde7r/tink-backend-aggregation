package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.investment.entities;

import java.util.Optional;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.system.rpc.Instrument;

@JsonObject
public class FundHolding {

    private FundHoldingDetail fundHoldingDetail;
    private String fundName;
    // Not sure that the following fields are always returned...
    // They are not part of Handelsbanken App, but seem to be part of the server response at the moment (2018-01-19)
    private String currency;
    private String isin;
    private String companyName;

    public Optional<Instrument> toInstrument() {
        if (fundHoldingDetail == null || fundHoldingDetail.hasNoValue()) {
            return Optional.empty();
        }

        return Optional.of(new Instrument())
                .map(this::applyTo)
                .flatMap(instrument -> Optional.ofNullable(fundHoldingDetail)
                        .map(fundHoldingDetail ->
                                fundHoldingDetail.applyTo(instrument)
                        )
                );
    }

    private Instrument applyTo(Instrument instrument) {
        instrument.setType(Instrument.Type.FUND);
        instrument.setCurrency(currency);
        instrument.setRawType(companyName);
        instrument.setName(fundName);
        instrument.setUniqueIdentifier(isin);
        instrument.setIsin(isin);
        return instrument;
    }

}
