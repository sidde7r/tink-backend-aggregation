package se.tink.backend.aggregation.nxgen.raw_data_events.event_producers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.nxgen.raw_data_events.configuration.RawBankDataEventCreationStrategies;
import se.tink.backend.aggregation.nxgen.raw_data_events.event_producers.pojo.FieldData;
import se.tink.backend.aggregation.nxgen.raw_data_events.event_producers.pojo.FieldPathPart;
import se.tink.backend.aggregation.nxgen.raw_data_events.masking.keys.RawBankDataKeyValueMaskingStrategy;
import se.tink.backend.aggregation.nxgen.raw_data_events.masking.values.RawBankDataFieldValueMaskingStrategy;
import se.tink.backend.aggregation.nxgen.raw_data_events.type_detection.RawBankDataFieldTypeDetectionStrategy;
import se.tink.eventproducerservice.events.grpc.RawBankDataTrackerEventProto.RawBankDataTrackerEvent;
import se.tink.eventproducerservice.events.grpc.RawBankDataTrackerEventProto.RawBankDataTrackerEventBankField;
import se.tink.eventproducerservice.events.grpc.RawBankDataTrackerEventProto.RawBankDataTrackerEventBankFieldType;
import se.tink.libraries.serialization.proto.utils.ProtobufTypeUtil;

@AllArgsConstructor
public class DefaultRawBankDataEventProducer implements RawBankDataEventProducer {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(DefaultRawBankDataEventProducer.class);

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private RawBankDataEventCreationStrategies rawBankDataEventCreationStrategies;

    @Override
    public Optional<RawBankDataTrackerEvent> produceRawBankDataEvent(
            String responseBody, String correlationId, String providerName) {
        // Try to parse the response body as JSON, if it fails we will silently ignore it
        // and stop trying to emit event
        JsonNode node;
        try {
            node = MAPPER.readTree(responseBody);
        } catch (Exception e) {
            LOGGER.info(
                    "[DefaultRawBankDataEventProducer] Could not parse response body, this is not unexpected, most probably the response body is not in JSON format");
            return Optional.empty();
        }

        try {
            // Set the simple fields for the event
            RawBankDataTrackerEvent.Builder eventBuilder =
                    RawBankDataTrackerEvent.newBuilder()
                            .setTimestamp(ProtobufTypeUtil.toProtobufTimestamp(Instant.now()))
                            .setCorrelationId(correlationId)
                            .setProviderName(providerName);

            // Flatten the JSON response body
            List<FieldData> fieldDataList = new ArrayList<>();
            flattenJsonNode(
                    node,
                    new ArrayList<>(),
                    new ArrayList<>(Collections.singletonList("0")),
                    fieldDataList);

            for (FieldData fieldData : fieldDataList) {
                List<FieldPathPart> fieldPath = fieldData.getFieldPath();
                String fieldValue = fieldData.getFieldValue();
                JsonNodeType type = fieldData.getFieldType();
                RawBankDataTrackerEventBankFieldType fieldType =
                        getFieldType(
                                fieldPath,
                                fieldValue,
                                type,
                                rawBankDataEventCreationStrategies
                                        .getFieldTypeDetectionStrategies());
                String maskedFieldValue =
                        maskFieldValue(
                                fieldPath,
                                fieldValue,
                                fieldType,
                                rawBankDataEventCreationStrategies.getValueMaskingStrategies());
                String maskedFieldPath =
                        maskFieldKey(
                                fieldPath,
                                rawBankDataEventCreationStrategies.getKeyMaskingStrategies());
                boolean isFieldMasked = !(maskedFieldValue.equals(fieldValue));
                boolean isFieldSet = !(JsonNodeType.NULL.equals(type));

                eventBuilder.addFieldData(
                        RawBankDataTrackerEventBankField.newBuilder()
                                .setFieldPath(maskedFieldPath)
                                .setFieldType(fieldType)
                                .setIsFieldSet(isFieldSet)
                                .setIsFieldMasked(isFieldMasked)
                                .setFieldValue(maskedFieldValue)
                                .setOffset(String.join(",", fieldData.getOffsets()))
                                .build());
            }
            LOGGER.info("[DefaultRawBankDataEventProducer] Event produced successfully");
            return Optional.of(eventBuilder.build());
        } catch (Exception e) {
            LOGGER.warn(
                    "[DefaultRawBankDataEventProducer] Error while producing raw bank data event");
            return Optional.empty();
        }
    }

    @Override
    public void overrideRawBankDataEventCreationStrategies(
            RawBankDataEventCreationStrategies rawBankDataEventCreationStrategies) {
        this.rawBankDataEventCreationStrategies = rawBankDataEventCreationStrategies;
    }

    private static RawBankDataTrackerEventBankFieldType getFieldType(
            List<FieldPathPart> fieldPath,
            String fieldValue,
            JsonNodeType type,
            List<RawBankDataFieldTypeDetectionStrategy> detectors) {
        for (RawBankDataFieldTypeDetectionStrategy detector : detectors) {
            if (detector.isTypeMatched(fieldPath, fieldValue, type)) {
                return detector.getType(fieldPath, fieldValue, type);
            }
        }
        return RawBankDataTrackerEventBankFieldType.UNKNOWN;
    }

    private static String maskFieldKey(
            List<FieldPathPart> fieldPath,
            List<RawBankDataKeyValueMaskingStrategy> keyMaskingStrategies) {
        StringBuilder stringBuilder = new StringBuilder();
        for (FieldPathPart keyPart : fieldPath) {
            stringBuilder.append(maskFieldKeyPart(keyPart.getKeyName(), keyMaskingStrategies));
            if (keyPart.isKeyRepresentsArray()) {
                stringBuilder.append("[]");
            }
            stringBuilder.append(".");
        }
        String response = stringBuilder.toString();
        return response.substring(0, response.length() - 1);
    }

    private static String maskFieldKeyPart(
            String keyPart, List<RawBankDataKeyValueMaskingStrategy> keyMaskingStrategies) {
        for (RawBankDataKeyValueMaskingStrategy strategy : keyMaskingStrategies) {
            if (strategy.shouldMask(keyPart)) {
                return strategy.mask(keyPart);
            }
        }
        return keyPart;
    }

    private static String maskFieldValue(
            List<FieldPathPart> fieldPathParts,
            String fieldValue,
            RawBankDataTrackerEventBankFieldType fieldType,
            List<RawBankDataFieldValueMaskingStrategy> strategies) {
        for (RawBankDataFieldValueMaskingStrategy strategy : strategies) {
            if (strategy.shouldUseMaskingStrategy(fieldPathParts, fieldValue, fieldType)) {
                return strategy.produceMaskedValue(fieldPathParts, fieldValue, fieldType);
            }
        }
        return "MASKED_VALUE";
    }

    // Recursively iterate on all fields of JSON and collect key value pairs
    private static void flattenJsonNode(
            JsonNode node,
            List<FieldPathPart> keyPrefix,
            List<String> currentOffset,
            List<FieldData> fieldDataList) {
        JsonNodeType nodeType = node.getNodeType();
        if (JsonNodeType.POJO.equals(nodeType)
                || JsonNodeType.MISSING.equals(nodeType)
                || JsonNodeType.BINARY.equals(nodeType)) {
            throw new IllegalStateException("We don't know how to handle that");
        }
        if (JsonNodeType.NULL.equals(nodeType)
                || JsonNodeType.BOOLEAN.equals(nodeType)
                || JsonNodeType.NUMBER.equals(nodeType)
                || JsonNodeType.STRING.equals(nodeType)) {
            fieldDataList.add(
                    new FieldData(
                            new ArrayList<>(keyPrefix),
                            node.asText(),
                            nodeType,
                            new ArrayList<>(currentOffset)));
        }
        if (JsonNodeType.ARRAY.equals(nodeType)) {
            for (int i = 0; i < node.size(); i++) {
                List<FieldPathPart> keyPrefixClone =
                        keyPrefix.stream().map(FieldPathPart::new).collect(Collectors.toList());
                if (keyPrefixClone.isEmpty()) {
                    keyPrefixClone.add(new FieldPathPart("", true));
                } else {
                    int lastIndex = keyPrefixClone.size() - 1;
                    keyPrefixClone.get(lastIndex).setKeyRepresentsArray(true);
                }
                List<String> newOffsetList = new ArrayList<>(currentOffset);
                newOffsetList.add(Integer.toString(i));
                flattenJsonNode(node.get(i), keyPrefixClone, newOffsetList, fieldDataList);
            }
        }
        if (JsonNodeType.OBJECT.equals(nodeType)) {
            for (Iterator<String> it = node.fieldNames(); it.hasNext(); ) {
                String fieldName = it.next();
                List<FieldPathPart> keyPrefixClone =
                        keyPrefix.stream().map(FieldPathPart::new).collect(Collectors.toList());
                keyPrefixClone.add(new FieldPathPart(fieldName, false));
                flattenJsonNode(
                        node.get(fieldName),
                        keyPrefixClone,
                        new ArrayList<>(currentOffset),
                        fieldDataList);
            }
        }
    }
}
