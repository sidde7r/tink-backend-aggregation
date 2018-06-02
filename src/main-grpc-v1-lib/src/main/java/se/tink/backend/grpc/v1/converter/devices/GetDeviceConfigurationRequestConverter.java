package se.tink.backend.grpc.v1.converter.devices;

import java.util.UUID;
import se.tink.backend.grpc.v1.converter.Converter;
import se.tink.backend.rpc.GetDeviceConfigurationCommand;
import se.tink.grpc.v1.rpc.GetDeviceConfigurationRequest;
import se.tink.libraries.uuid.UUIDUtils;

public class GetDeviceConfigurationRequestConverter implements
        Converter<GetDeviceConfigurationRequest, GetDeviceConfigurationCommand> {

    @Override
    public GetDeviceConfigurationCommand convertFrom(GetDeviceConfigurationRequest input) {
        return new GetDeviceConfigurationCommand(input.getDeviceId(), input.getMarketCode());
    }
}
