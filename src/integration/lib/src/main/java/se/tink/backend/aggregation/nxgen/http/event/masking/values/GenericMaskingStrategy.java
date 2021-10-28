package se.tink.backend.aggregation.nxgen.http.event.masking.values;

import static se.tink.eventproducerservice.events.grpc.RawBankDataTrackerEventProto.RawBankDataTrackerEventBankFieldType.*;

import java.util.Arrays;
import java.util.List;
import se.tink.backend.aggregation.nxgen.http.event.event_producers.pojo.FieldPathPart;
import se.tink.eventproducerservice.events.grpc.RawBankDataTrackerEventProto.RawBankDataTrackerEventBankFieldType;

public class GenericMaskingStrategy implements RawBankDataFieldValueMaskingStrategy {

    /*
     Used to mask values for STRING, INTEGER, DOUBLE fields or fields that
     contain account identifier. The strategy is the following:

     1- If length of field is bigger or equal to 20 characters just output "<LONG_STRING>"
     2- Otherwise, go through each character in the string:
         a) If character is among ALLOWED_CHARACTERS_FOR_UNMASK then keep it as it is
         b) If it is a number, replace it with "D"
         c) Otherwise replace it with "L"
    */

    private static final List<RawBankDataTrackerEventBankFieldType> ACCOUNT_IDENTIFIER_FIELD_TYPES =
            Arrays.asList(IBAN, BBAN, SORT_CODE, PAYMENT_ACCOUNT_NUMBER, MASKED_PAN);

    private static final List<RawBankDataTrackerEventBankFieldType> ALLOWED_FIELDS_FOR_STRATEGY =
            Arrays.asList(STRING, DOUBLE, INTEGER);

    private static final List<Character> ALLOWED_CHARACTERS_FOR_UNMASK =
            Arrays.asList('*', '-', '_', '.', ',', ' ');

    @Override
    public boolean shouldUseMaskingStrategy(
            List<FieldPathPart> fieldPathParts,
            String value,
            RawBankDataTrackerEventBankFieldType fieldType) {
        return ALLOWED_FIELDS_FOR_STRATEGY.contains(fieldType)
                || ACCOUNT_IDENTIFIER_FIELD_TYPES.contains(fieldType);
    }

    @Override
    public String produceMaskedValue(
            List<FieldPathPart> fieldPathParts,
            String value,
            RawBankDataTrackerEventBankFieldType fieldType) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (ALLOWED_CHARACTERS_FOR_UNMASK.contains(c)) {
                builder.append(c);
            } else if (Character.isDigit(c)) {
                builder.append("D");
            } else {
                builder.append("L");
            }
        }
        return builder.toString();
    }
}
