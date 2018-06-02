package se.tink.backend.grpc.v1.converter.application;

import se.tink.backend.core.ApplicationFieldError;
import se.tink.backend.grpc.v1.converter.ConverterUtils;
import se.tink.grpc.v1.models.ApplicationFormFieldError;

import java.util.List;
import java.util.stream.Collectors;

public class ApplicationFormFieldErrorConverter {

    public static ApplicationFormFieldError convert(ApplicationFieldError input) {
        ApplicationFormFieldError.Builder builder = ApplicationFormFieldError.newBuilder();
        ConverterUtils.setIfPresent(input::getMessage, builder::setMessage);
        return builder.build();
    }

    public static List<ApplicationFormFieldError> convert(List<ApplicationFieldError> input) {
        return input.stream().map(ApplicationFormFieldErrorConverter::convert).collect(Collectors.toList());
    }

}
