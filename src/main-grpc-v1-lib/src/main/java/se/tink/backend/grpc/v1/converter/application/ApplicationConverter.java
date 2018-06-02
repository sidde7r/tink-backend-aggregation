package se.tink.backend.grpc.v1.converter.application;

import se.tink.backend.grpc.v1.converter.Converter;
import se.tink.backend.grpc.v1.converter.ConverterUtils;
import se.tink.backend.grpc.v1.converter.EnumMappers;
import se.tink.grpc.v1.models.Application;
import se.tink.grpc.v1.models.ApplicationForms;
import se.tink.grpc.v1.models.ApplicationType;

import java.util.UUID;

public class ApplicationConverter implements Converter<se.tink.backend.core.Application, Application> {
    @Override
    public Application convertFrom(se.tink.backend.core.Application input) {
        Application.Builder builder = Application.newBuilder();
        ConverterUtils.setIfPresent(input::getId, builder::setId, UUID::toString);
        ConverterUtils.setIfPresent(input::getForms, builder::setForms,
                forms -> ApplicationForms.newBuilder().addAllForm(ApplicationFormConverter.convert(forms)));
        ConverterUtils.setIfPresent(input::getStatus, builder::setStatus, ApplicationStatusConverter::convert);
        ConverterUtils.setIfPresent(input::getSteps, builder::setSteps);
        ConverterUtils.setIfPresent(input::getTitle, builder::setTitle);
        builder.setType(EnumMappers.APPLICATION_TYPE_TO_GRPC.getOrDefault(input.getType(), ApplicationType.APPLICATION_TYPE_UNKNOWN));
        return builder.build();
    }
}
