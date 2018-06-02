package se.tink.backend.grpc.v1.converter.authentication.keys;

import se.tink.backend.grpc.v1.converter.Converter;
import se.tink.backend.grpc.v1.converter.ConverterUtils;
import se.tink.backend.grpc.v1.converter.EnumMappers;
import se.tink.backend.rpc.auth.keys.AuthenticationKeyResponse;
import se.tink.grpc.v1.models.AuthenticationKey;
import se.tink.grpc.v1.models.AuthenticationKeyType;
import se.tink.grpc.v1.models.AuthenticationSource;

public class AuthenticationKeyResponseConverter implements Converter<AuthenticationKeyResponse,
        AuthenticationKey> {
    @Override
    public AuthenticationKey convertFrom(AuthenticationKeyResponse input) {
        AuthenticationKey.Builder builder = AuthenticationKey.newBuilder();
        ConverterUtils.setIfPresent(input::getId, builder::setId);
        ConverterUtils.setIfPresent(input::getKey, builder::setKey);
        ConverterUtils.setIfPresent(input::getSource, builder::setSource,
                source -> EnumMappers.CORE_AUTHENTICATION_SOURCE_TO_GRPC_MAP.getOrDefault(source,
                        AuthenticationSource.AUTHENTICATION_SOURCE_UNKNOWN));
        ConverterUtils.setIfPresent(input::getType, builder::setKeyType,
                type -> EnumMappers.CORE_KEY_TYPE_TO_GRPC_MAP.getOrDefault(type,
                        AuthenticationKeyType.KEY_TYPE_UNKNOWN));
        return builder.build();
    }
}
