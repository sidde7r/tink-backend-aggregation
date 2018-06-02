package se.tink.backend.grpc.v1.converter.devices;

import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.core.DeviceConfiguration;
import se.tink.backend.grpc.v1.converter.Converter;
import se.tink.grpc.v1.rpc.DeviceConfigurationResponse;

public class DeviceConfigurationConverter implements Converter<DeviceConfiguration, DeviceConfigurationResponse> {

    @Override
    public DeviceConfigurationResponse convertFrom(DeviceConfiguration input) {
        List<String> flags = input.getFeatureFlags();
        DeviceConfigurationMarketConverter converter = new DeviceConfigurationMarketConverter();
        List<DeviceConfigurationResponse.DeviceConfigurationMarket> markets = input.getMarkets().stream()
                .map(converter::convertFrom)
                .collect(Collectors.toList());
        return DeviceConfigurationResponse.newBuilder()
                .addAllFeatureFlags(flags)
                .addAllMarkets(markets)
                .build();
    }

}
