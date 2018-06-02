package se.tink.backend.grpc.v1.converter.user;

import se.tink.backend.grpc.v1.converter.Converter;
import se.tink.backend.grpc.v1.converter.ConverterUtils;
import se.tink.grpc.v1.models.UserConfiguration;

public class FirehoseUserConfigurationToGrpcUserConfigurationConverter
        implements Converter<se.tink.backend.firehose.v1.models.UserConfiguration, UserConfiguration> {
    private final FirehoseI18NConfigurationConverterToGrpcI18NConfigurationConverter u18nConfigurationConverter =
            new FirehoseI18NConfigurationConverterToGrpcI18NConfigurationConverter();

    @Override
    public UserConfiguration convertFrom(se.tink.backend.firehose.v1.models.UserConfiguration input) {
        UserConfiguration.Builder builder = UserConfiguration.newBuilder();
        ConverterUtils.setIfPresent(input::getFlagsList, builder::addAllFlags);
        ConverterUtils.setIfPresent(input::getI18NConfiguration, builder::setI18NConfiguration,
                u18nConfigurationConverter::convertFrom);
        return builder.build();
    }
}
