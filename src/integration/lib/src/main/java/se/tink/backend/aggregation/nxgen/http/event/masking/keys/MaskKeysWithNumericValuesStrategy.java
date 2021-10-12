package se.tink.backend.aggregation.nxgen.http.event.masking.keys;

public class MaskKeysWithNumericValuesStrategy implements RawBankDataKeyValueMaskingStrategy {

    @Override
    public boolean shouldMask(String key) {
        return key.matches(".*\\d.*");
    }

    @Override
    public String mask(String key) {
        return "MASKED";
    }
}
