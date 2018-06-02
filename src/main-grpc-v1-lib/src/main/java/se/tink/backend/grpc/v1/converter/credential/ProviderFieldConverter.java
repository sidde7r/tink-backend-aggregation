package se.tink.backend.grpc.v1.converter.credential;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.core.Field;
import se.tink.backend.grpc.v1.converter.ConverterUtils;
import se.tink.grpc.v1.models.ProviderFieldSpecification;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class ProviderFieldConverter {

    private static final TypeReference<List<Field>> LIST_OF_FIELDS_TYPE_REFERENCE = new TypeReference<List<Field>>() {
    };

    public static ProviderFieldSpecification convertFrom(Field input) {
        ProviderFieldSpecification.Builder builder = ProviderFieldSpecification.newBuilder();
        ConverterUtils.setIfPresent(input::getDescription, builder::setDescription);
        ConverterUtils.setIfPresent(input::getHint, builder::setHint);
        ConverterUtils.setIfPresent(input::getMaxLength, builder::setMaxLength);
        ConverterUtils.setIfPresent(input::getMinLength, builder::setMinLength);
        ConverterUtils.setIfPresent(input::isMasked, builder::setMasked);
        ConverterUtils.setIfPresent(input::isNumeric, builder::setNumeric);
        ConverterUtils.setIfPresent(input::isImmutable, builder::setImmutable);
        ConverterUtils.setIfPresent(input::isOptional, builder::setOptional);
        ConverterUtils.setIfPresent(input::getName, builder::setName);
        ConverterUtils.setIfPresent(input::getValue, builder::setValue);
        ConverterUtils.setIfPresent(input::getPattern, builder::setPattern);
        ConverterUtils.setIfPresent(input::getPatternError, builder::setPatternError);
        ConverterUtils.setIfPresent(input::getHelpText, builder::setHelpText);

        return builder.build();
    }

    static List<Field> deserializeFields(String input) {
        return SerializationUtils.deserializeFromString(input, LIST_OF_FIELDS_TYPE_REFERENCE);
    }

    static List<ProviderFieldSpecification> convertProviderFields(List<Field> list) {
        if (list == null) {
            return Lists.newArrayList();
        }

        return list.stream().map(ProviderFieldConverter::convertFrom).collect(Collectors.toList());
    }
}
