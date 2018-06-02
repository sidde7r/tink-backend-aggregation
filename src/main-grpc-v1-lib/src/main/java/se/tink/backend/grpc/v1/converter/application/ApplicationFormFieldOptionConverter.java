package se.tink.backend.grpc.v1.converter.application;

import se.tink.backend.core.ApplicationFieldOption;
import se.tink.backend.grpc.v1.converter.ConverterUtils;
import se.tink.grpc.v1.models.ApplicationFormFieldOption;

import java.util.List;
import java.util.stream.Collectors;

public class ApplicationFormFieldOptionConverter {


    public static ApplicationFormFieldOption convert(ApplicationFieldOption input) {
        ApplicationFormFieldOption.Builder builder = ApplicationFormFieldOption.newBuilder();

        ConverterUtils.setIfPresent(input::getValue, builder::setValue);
        ConverterUtils.setIfPresent(input::getLabel, builder::setLabel);
        ConverterUtils.setIfPresent(input::getDescription, builder::setDescription);
        ConverterUtils.setIfPresent(input::getSerializedPayload, builder::setSerializedPayload);
        return builder.build();
    }

    public static List<ApplicationFormFieldOption> convert(List<ApplicationFieldOption> input) {
        return input.stream().map(ApplicationFormFieldOptionConverter::convert).collect(Collectors.toList());
    }


}
