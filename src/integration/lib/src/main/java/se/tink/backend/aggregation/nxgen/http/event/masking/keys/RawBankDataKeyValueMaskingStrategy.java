package se.tink.backend.aggregation.nxgen.http.event.masking.keys;

public interface RawBankDataKeyValueMaskingStrategy {
    boolean shouldMask(String key);

    String mask(String key);
}
