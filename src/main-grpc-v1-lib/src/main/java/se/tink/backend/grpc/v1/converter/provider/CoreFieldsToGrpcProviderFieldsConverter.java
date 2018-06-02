package se.tink.backend.grpc.v1.converter.provider;

import se.tink.backend.core.Field;
import se.tink.backend.grpc.v1.converter.Converter;
import se.tink.backend.grpc.v1.converter.ConverterUtils;
import se.tink.grpc.v1.models.ProviderFieldSpecification;

public class CoreFieldsToGrpcProviderFieldsConverter implements Converter<Field, ProviderFieldSpecification> {
    @Override
    public ProviderFieldSpecification convertFrom(Field input) {
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
}
