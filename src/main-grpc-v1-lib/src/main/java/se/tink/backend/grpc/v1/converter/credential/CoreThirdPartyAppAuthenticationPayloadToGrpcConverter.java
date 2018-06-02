package se.tink.backend.grpc.v1.converter.credential;

import se.tink.backend.common.payloads.ThirdPartyAppAuthenticationPayload;
import se.tink.backend.grpc.v1.converter.Converter;
import se.tink.backend.grpc.v1.converter.ConverterUtils;
import se.tink.grpc.v1.models.ThirdPartyAppAuthentication;

public class CoreThirdPartyAppAuthenticationPayloadToGrpcConverter
        implements Converter<ThirdPartyAppAuthenticationPayload, ThirdPartyAppAuthentication> {

    @Override
    public ThirdPartyAppAuthentication convertFrom(ThirdPartyAppAuthenticationPayload input) {
        ThirdPartyAppAuthentication.Builder builder = ThirdPartyAppAuthentication.newBuilder();

        ConverterUtils.setIfPresent(input::getDownloadTitle, builder::setDownloadTitle);
        ConverterUtils.setIfPresent(input::getDownloadMessage, builder::setDownloadMessage);
        ConverterUtils.setIfPresent(input::getUpgradeTitle, builder::setUpgradeTitle);
        ConverterUtils.setIfPresent(input::getUpgradeMessage, builder::setUpgradeMessage);

        if (input.getIos() != null) {
            builder.setIos(convert(input.getIos()));
        }

        if (input.getAndroid() != null) {
            builder.setAndroid(convert(input.getAndroid()));
        }

        return builder.build();
    }

    private ThirdPartyAppAuthentication.Ios convert(ThirdPartyAppAuthenticationPayload.Ios ios) {
        ThirdPartyAppAuthentication.Ios.Builder iosBuilder = ThirdPartyAppAuthentication.Ios.newBuilder();
        ConverterUtils.setIfPresent(ios::getAppStoreUrl, iosBuilder::setAppStoreUrl);
        ConverterUtils.setIfPresent(ios::getScheme, iosBuilder::setScheme);
        ConverterUtils.setIfPresent(ios::getDeepLinkUrl, iosBuilder::setDeepLinkUrl);
        return iosBuilder.build();
    }

    private ThirdPartyAppAuthentication.Android convert(ThirdPartyAppAuthenticationPayload.Android android) {
        ThirdPartyAppAuthentication.Android.Builder androidBuilder = ThirdPartyAppAuthentication.Android.newBuilder();
        ConverterUtils.setIfPresent(android::getPackageName, androidBuilder::setPackageName);
        ConverterUtils.setIfPresent(android::getRequiredMinimumVersion, androidBuilder::setRequiredMinimumVersion);
        ConverterUtils.setIfPresent(android::getIntent, androidBuilder::setIntent);
        return androidBuilder.build();
    }
}
