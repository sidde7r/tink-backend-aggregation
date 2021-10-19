package se.tink.backend.aggregation.nxgen.http.event.masking.values;

import java.util.List;
import se.tink.backend.aggregation.nxgen.http.event.event_producers.pojo.FieldPathPart;
import se.tink.eventproducerservice.events.grpc.RawBankDataTrackerEventProto.RawBankDataTrackerEventBankFieldType;

public class UnmaskDateValueMaskingStrategy implements RawBankDataFieldValueMaskingStrategy {

  @Override
  public boolean shouldMask(List<FieldPathPart> fieldPathParts, String value,
      RawBankDataTrackerEventBankFieldType fieldType) {
    return RawBankDataTrackerEventBankFieldType.DATE.equals(fieldType);
  }

  @Override
  public String mask(List<FieldPathPart> fieldPathParts, String value,
      RawBankDataTrackerEventBankFieldType fieldType) {
    return value;
  }
}
