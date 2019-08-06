package se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.instrument.id;

public interface InstrumentUniqueIdStep<T> {
    /**
     * @param uniqueIdentifier Normally the uniqueIdentifier should be isin + market. If isin and
     *     market is hard to get hold of and the bank / broker have some other way to identify the
     *     instrument we can use that.
     */
    InstrumentNameStep<T> withUniqueIdentifier(String uniqueIdentifier);
}
