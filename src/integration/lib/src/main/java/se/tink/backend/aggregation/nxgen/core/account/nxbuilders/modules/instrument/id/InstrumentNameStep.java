package se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.instrument.id;

public interface InstrumentNameStep<T> {
    /** @param name instrument name e.g. 'Apple Inc.' */
    T withName(String name);
}
