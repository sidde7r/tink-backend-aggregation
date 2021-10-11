package se.tink.backend.aggregation.nxgen.http.event.masking.values;

import java.util.List;
import se.tink.backend.aggregation.nxgen.http.event.event_producers.pojo.FieldPathPart;

public class MaskAllValueMaskingStrategy implements RawBankDataFieldValueMaskingStrategy {

    @Override
    public boolean shouldMask(List<FieldPathPart> fieldPathParts, String value) {
        return true;
    }

    @Override
    public String mask(List<FieldPathPart> fieldPathParts, String value) {
        return "MASKED";
    }
}
