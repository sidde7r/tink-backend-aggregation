package se.tink.backend.aggregation.nxgen.raw_data_events.masking.keys;

public interface RawBankDataKeyValueMaskingStrategy {
    boolean shouldMask(String key);

    String mask(String key);
}
