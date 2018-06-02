package se.tink.backend.grpc.v1.converter.application;

import se.tink.backend.core.enums.ApplicationFormType;
import se.tink.backend.grpc.v1.converter.ConverterUtils;
import se.tink.grpc.v1.models.ApplicationForm;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import se.tink.grpc.v1.models.ApplicationFormFields;

public class ApplicationFormConverter {

    public static ApplicationForm convert(se.tink.backend.core.ApplicationForm input) {
        ApplicationForm.Builder builder = ApplicationForm.newBuilder();

        ConverterUtils.setIfPresent(input::getId, builder::setId, UUID::toString);
        ConverterUtils.setIfPresent(input::getApplicationId, builder::setApplicationId, UUID::toString);
        ConverterUtils.setIfPresent(input::getDescription, builder::setDescription);
        ConverterUtils.setIfPresent(input::getFields, builder::setFields, fields -> ApplicationFormFields.newBuilder()
                .addAllField(ApplicationFormFieldConverter.convert(fields)));
        ConverterUtils.setIfPresent(input::getStatus, builder::setStatus, ApplicationFormStatusConverter::convert);
        ConverterUtils.setIfPresent(input::getType, builder::setType, ApplicationFormType::toString);
        ConverterUtils.setIfPresent(input::getTitle, builder::setTitle);
        ConverterUtils.setIfPresent(input::getSerializedPayload, builder::setSerializedPayload);
        ConverterUtils.setIfPresent(input::getName, builder::setName);

        return builder.build();
    }

    public static List<ApplicationForm> convert(List<se.tink.backend.core.ApplicationForm> input) {
        return input.stream().map(ApplicationFormConverter::convert).collect(Collectors.toList());
    }
}
