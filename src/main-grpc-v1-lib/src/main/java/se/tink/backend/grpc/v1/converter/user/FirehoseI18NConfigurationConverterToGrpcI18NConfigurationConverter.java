package se.tink.backend.grpc.v1.converter.user;

import se.tink.backend.grpc.v1.converter.Converter;
import se.tink.backend.grpc.v1.converter.ConverterUtils;
import se.tink.grpc.v1.models.UserConfiguration;

public class FirehoseI18NConfigurationConverterToGrpcI18NConfigurationConverter implements
        Converter<se.tink.backend.firehose.v1.models.UserConfiguration.I18NConfiguration, UserConfiguration.I18NConfiguration> {
    @Override
    public UserConfiguration.I18NConfiguration convertFrom(
            se.tink.backend.firehose.v1.models.UserConfiguration.I18NConfiguration i18NConfiguration) {
        UserConfiguration.I18NConfiguration.Builder builder = UserConfiguration.I18NConfiguration.newBuilder();
        ConverterUtils.setIfPresent(i18NConfiguration::getCurrencyCode, builder::setCurrencyCode);
        ConverterUtils.setIfPresent(i18NConfiguration::getLocaleCode, builder::setLocaleCode);
        ConverterUtils.setIfPresent(i18NConfiguration::getMarketCode, builder::setMarketCode);
        ConverterUtils.setIfPresent(i18NConfiguration::getTimezoneCode, builder::setTimezoneCode);
        return builder.build();
    }
}
