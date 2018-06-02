package se.tink.backend.grpc.v1.converter.devices;

import se.tink.backend.grpc.v1.converter.Converter;
import se.tink.backend.grpc.v1.converter.ConverterUtils;
import se.tink.grpc.v1.models.Device;

public class CoreDeviceToGrpcDevice implements Converter<se.tink.backend.core.Device, Device> {
    @Override
    public Device convertFrom(se.tink.backend.core.Device input) {
        Device.Builder builder = Device.newBuilder();
        ConverterUtils.setIfPresent(input::getDeviceToken, builder::setId);
        ConverterUtils.setIfPresent(input::getNotificationToken, builder::setNotificationToken);
        ConverterUtils.setIfPresent(input::getPublicKey, builder::setNotificationPublicKey);
        return builder.build();
    }
}
