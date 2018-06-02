package se.tink.backend.grpc.v1.converter.user;

import se.tink.backend.core.User;
import se.tink.backend.grpc.v1.converter.Converter;
import se.tink.backend.grpc.v1.converter.ConverterUtils;
import se.tink.grpc.v1.models.UserConfiguration;

public class CoreUserToUserConfigurationConverter implements Converter<User, UserConfiguration> {
    private final CoreUserProfileToI18NConfigurationConverter u18nConfigurationConverter = new CoreUserProfileToI18NConfigurationConverter();

    @Override
    public UserConfiguration convertFrom(User input) {
        UserConfiguration.Builder builder = UserConfiguration.newBuilder();
        ConverterUtils.setIfPresent(input::getFlags, builder::addAllFlags);
        ConverterUtils.setIfPresent(input::getProfile, builder::setI18NConfiguration,
                u18nConfigurationConverter::convertFrom);
        ConverterUtils.setIfPresent(input::getId, builder::setUserId);
        return builder.build();
    }
}
