package se.tink.backend.grpc.v1.converter.application;

import se.tink.backend.core.ApplicationField;
import se.tink.backend.grpc.v1.converter.ConverterUtils;
import se.tink.backend.grpc.v1.converter.EnumMappers;
import se.tink.backend.grpc.v1.utils.ProtobufModelUtils;
import se.tink.grpc.v1.models.ApplicationFieldType;
import se.tink.grpc.v1.models.ApplicationFormField;

import java.util.List;
import java.util.stream.Collectors;
import se.tink.grpc.v1.models.ApplicationFormFieldErrors;
import se.tink.grpc.v1.models.ApplicationFormFieldOptions;

public class ApplicationFormFieldConverter {

    public static ApplicationFormField convert(ApplicationField input) {
        ApplicationFormField.Builder builder = ApplicationFormField.newBuilder();

        if (input.getDefaultValue() != null) {
            builder.setDefaultValue(ProtobufModelUtils.toStringValue(input.getDefaultValue()));
        }
        ConverterUtils.setIfPresent(input::getDescription, builder::setDescription);
        ConverterUtils.setIfPresent(input::getErrors, builder::setErrors,
                errors -> ApplicationFormFieldErrors.newBuilder()
                        .addAllError(ApplicationFormFieldErrorConverter.convert(errors)).build());
        ConverterUtils.setIfPresent(input::getLabel, builder::setLabel);
        ConverterUtils.setIfPresent(input::getName, builder::setName);
        ConverterUtils.setIfPresent(input::getOptions, builder::setOptions,
                options -> ApplicationFormFieldOptions.newBuilder()
                        .addAllOption(ApplicationFormFieldOptionConverter.convert(options)).build());
        ConverterUtils.setIfPresent(input::getPattern, builder::setPattern);
        ConverterUtils.setIfPresent(input::getType, builder::setType, type ->
                EnumMappers.APPLICATION_FIELD_TYPE_TO_GRPC.getOrDefault(type, ApplicationFieldType.APPLICATION_FIELD_TYPE_UNKNOWN));
        if (input.getValue() != null) {
            builder.setValue(ProtobufModelUtils.toStringValue(input.getValue()));
        }
        ConverterUtils.setIfPresent(input::isReadOnly, builder::setReadOnly);
        ConverterUtils.setIfPresent(input::isRequired, builder::setRequired);
        ConverterUtils.setIfPresent(input::getDependency, builder::setDependency);
        ConverterUtils.setIfPresent(input::getInfoBody, builder::setInfoBody);
        ConverterUtils.setIfPresent(input::getInfoTitle, builder::setInfoTitle);
        ConverterUtils.setIfPresent(input::getIntroduction, builder::setIntroduction);

        return builder.build();
    }

    public static List<ApplicationFormField> convert(List<ApplicationField> input) {
        return input.stream().map(ApplicationFormFieldConverter::convert).collect(Collectors.toList());
    }


}
