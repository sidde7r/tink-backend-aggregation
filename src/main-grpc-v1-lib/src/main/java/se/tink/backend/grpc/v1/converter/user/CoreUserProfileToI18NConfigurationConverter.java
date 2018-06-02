package se.tink.backend.grpc.v1.converter.user;

import se.tink.backend.core.UserProfile;
import se.tink.backend.grpc.v1.converter.Converter;
import se.tink.backend.grpc.v1.converter.ConverterUtils;
import se.tink.grpc.v1.models.UserConfiguration;

public class CoreUserProfileToI18NConfigurationConverter implements Converter<UserProfile, UserConfiguration.I18NConfiguration> {
    @Override
    public UserConfiguration.I18NConfiguration convertFrom(UserProfile userProfile) {
        UserConfiguration.I18NConfiguration.Builder builder = UserConfiguration.I18NConfiguration.newBuilder();
        ConverterUtils.setIfPresent(userProfile::getCurrency, builder::setCurrencyCode);
        ConverterUtils.setIfPresent(userProfile::getLocale, builder::setLocaleCode);
        ConverterUtils.setIfPresent(userProfile::getMarket, builder::setMarketCode);
        ConverterUtils.setIfPresent(userProfile::getTimeZone, builder::setTimezoneCode);
        return builder.build();
    }
}
