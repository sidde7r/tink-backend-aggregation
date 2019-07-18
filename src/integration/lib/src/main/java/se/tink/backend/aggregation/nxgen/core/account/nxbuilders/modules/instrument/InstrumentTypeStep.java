package se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.instrument;

import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.instrument.InstrumentModule.InstrumentType;

public interface InstrumentTypeStep<T> {
    /** @param type instrument type mapped to {@link InstrumentType} */
    InstrumentIdStep<T> withType(InstrumentType type);
}
