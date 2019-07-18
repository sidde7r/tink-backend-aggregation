package se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.instrument;

public interface InstrumentBuildStep {
    /** @param rawType the instrument type as received from the bank */
    InstrumentBuildStep setRawType(String rawType);

    InstrumentModule build();
}
