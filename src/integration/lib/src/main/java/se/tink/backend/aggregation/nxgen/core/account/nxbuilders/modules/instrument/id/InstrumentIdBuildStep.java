package se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.instrument.id;

public interface InstrumentIdBuildStep {
    /** @param marketPlace instrument stock market place e.g. 'NASDAQ' */
    InstrumentIdBuildStep setMarketPlace(String marketPlace);

    InstrumentIdModule build();
}
