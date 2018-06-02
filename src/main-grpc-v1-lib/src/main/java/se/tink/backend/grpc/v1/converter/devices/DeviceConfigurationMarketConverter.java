package se.tink.backend.grpc.v1.converter.devices;

import java.util.stream.Collectors;
import se.tink.backend.core.Market;
import se.tink.backend.grpc.v1.converter.Converter;
import se.tink.backend.grpc.v1.converter.ConverterUtils;
import se.tink.backend.grpc.v1.converter.EnumMappers;
import se.tink.grpc.v1.models.AuthenticationMethod;
import se.tink.grpc.v1.rpc.DeviceConfigurationResponse;

public class DeviceConfigurationMarketConverter implements
        Converter<Market, DeviceConfigurationResponse.DeviceConfigurationMarket> {
    @Override
    public DeviceConfigurationResponse.DeviceConfigurationMarket convertFrom(Market input) {
        DeviceConfigurationResponse.DeviceConfigurationMarket.Builder builder = DeviceConfigurationResponse.DeviceConfigurationMarket
                .newBuilder();

        ConverterUtils.setIfPresent(input::getCodeAsString, builder::setMarketCode);
        ConverterUtils.setIfPresent(input::getDescription, builder::setLabel);
        ConverterUtils.setIfPresent(input::getLinkToAboutPage, builder::setLinkToAboutPage);
        ConverterUtils.setIfPresent(input::getLinkToHelpPage, builder::setLinkToHelpPage);
        ConverterUtils.setIfPresent(input::getLinkToSecurityPage, builder::setLinkToSecurityPage);
        ConverterUtils.setIfPresent(input::getLinkToTermsOfServicePage, builder::setLinkToTermsOfServicePage);

        builder.addAllLoginMethods(input.getLoginMethods().stream()
                .map(loginMethod -> EnumMappers.AUTHENTICATION_METHOD_TO_GRPC
                        .getOrDefault(loginMethod, AuthenticationMethod.AUTHENTICATION_METHOD_UNKNOWN))
                .collect(Collectors.toList()));

        builder.addAllRegisterMethods(input.getRegisterMethods().stream()
                .map(registerMethod -> EnumMappers.AUTHENTICATION_METHOD_TO_GRPC
                        .getOrDefault(registerMethod, AuthenticationMethod.AUTHENTICATION_METHOD_UNKNOWN))
                .collect(Collectors.toList()));

        builder.setSuggested(input.isSuggested());
        return builder.build();
    }
}
