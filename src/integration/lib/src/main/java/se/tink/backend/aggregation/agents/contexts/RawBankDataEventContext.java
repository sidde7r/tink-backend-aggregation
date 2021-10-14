package se.tink.backend.aggregation.agents.contexts;

import se.tink.backend.aggregation.nxgen.http.event.event_producers.RawBankDataEventAccumulator;

public interface RawBankDataEventContext {

    void setRawBankDataEventAccumulator(RawBankDataEventAccumulator rawBankDataEventAccumulator);

    RawBankDataEventAccumulator getRawBankDataEventAccumulator();
}
