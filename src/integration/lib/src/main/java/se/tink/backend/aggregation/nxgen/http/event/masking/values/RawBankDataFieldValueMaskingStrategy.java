package se.tink.backend.aggregation.nxgen.http.event.masking.values;

import java.util.List;
import se.tink.backend.aggregation.nxgen.http.event.event_producers.pojo.FieldPathPart;

public interface RawBankDataFieldValueMaskingStrategy {

    boolean shouldMask(List<FieldPathPart> fieldPathParts, String value);

    String mask(List<FieldPathPart> fieldPathParts, String value);
}
