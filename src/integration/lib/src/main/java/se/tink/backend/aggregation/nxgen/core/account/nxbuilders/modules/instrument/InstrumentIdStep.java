package se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.instrument;

import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.instrument.id.InstrumentIdModule;

public interface InstrumentIdStep<T> {
    MarketPriceStep<T> withId(InstrumentIdModule instrumentIdModule);
}
