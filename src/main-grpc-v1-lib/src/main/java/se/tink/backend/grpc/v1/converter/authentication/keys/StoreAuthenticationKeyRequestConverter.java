package se.tink.backend.grpc.v1.converter.authentication.keys;

import se.tink.backend.grpc.v1.converter.Converter;
import se.tink.backend.grpc.v1.converter.ConverterUtils;
import se.tink.backend.grpc.v1.converter.EnumMappers;
import se.tink.backend.rpc.auth.keys.StoreAuthenticationKeyCommand;
import se.tink.grpc.v1.rpc.StoreAuthenticationKeyRequest;

public class StoreAuthenticationKeyRequestConverter implements Converter<StoreAuthenticationKeyRequest,
        StoreAuthenticationKeyCommand> {

    @Override
    public StoreAuthenticationKeyCommand convertFrom(StoreAuthenticationKeyRequest request) {
        StoreAuthenticationKeyCommand command = new StoreAuthenticationKeyCommand();
        ConverterUtils.setIfPresent(request::getAuthenticationToken, command::setAuthenticationToken);
        ConverterUtils.setIfPresent(request::getKey, command::setKey);
        ConverterUtils.setIfPresent(request::getSource, command::setSource,
                source -> EnumMappers.CORE_AUTHENTICATION_SOURCE_TO_GRPC_MAP.inverse().get(source));
        ConverterUtils.setIfPresent(request::getKeyType, command::setType,
                type -> EnumMappers.CORE_KEY_TYPE_TO_GRPC_MAP.inverse().get(type));
        return command;
    }
}
