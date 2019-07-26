package se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.portfolio;

import java.util.List;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.instrument.InstrumentModule;

public interface WithInstrumentStep<T> {

    T withInstruments(InstrumentModule... instrumentModules);

    T withInstruments(List<InstrumentModule> instrumentModules);
}
