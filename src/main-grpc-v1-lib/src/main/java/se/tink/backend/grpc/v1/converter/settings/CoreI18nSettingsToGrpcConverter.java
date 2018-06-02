package se.tink.backend.grpc.v1.converter.settings;

import se.tink.backend.core.I18nSettings;
import se.tink.backend.grpc.v1.converter.Converter;
import se.tink.backend.grpc.v1.converter.ConverterUtils;
import se.tink.grpc.v1.models.I18NSettings;

public class CoreI18nSettingsToGrpcConverter implements Converter<I18nSettings, I18NSettings> {
    @Override
    public I18NSettings convertFrom(I18nSettings input) {
        I18NSettings.Builder builder = I18NSettings.newBuilder();
        ConverterUtils.setIfPresent(input::getLocaleCode, builder::setLocaleCode);
        return builder.build();
    }
}
