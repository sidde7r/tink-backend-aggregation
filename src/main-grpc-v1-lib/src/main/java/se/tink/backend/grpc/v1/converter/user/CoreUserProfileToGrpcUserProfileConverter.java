package se.tink.backend.grpc.v1.converter.user;

import java.util.stream.Collectors;
import se.tink.backend.grpc.v1.converter.Converter;
import se.tink.backend.grpc.v1.converter.ConverterUtils;
import se.tink.backend.grpc.v1.converter.EnumMappers;
import se.tink.backend.grpc.v1.utils.ProtobufModelUtils;
import se.tink.backend.rpc.UserProfileResponse;
import se.tink.grpc.v1.models.AuthenticationMethod;
import se.tink.grpc.v1.models.UserProfile;

public class CoreUserProfileToGrpcUserProfileConverter implements Converter<UserProfileResponse, UserProfile> {
    @Override
    public UserProfile convertFrom(UserProfileResponse input) {
        UserProfile.Builder builder = UserProfile.newBuilder();
        ConverterUtils.setIfPresent(input::getUsername, builder::setUsername);
        ConverterUtils.setIfPresent(input::getNationalId, builder::setNationalId);
        ConverterUtils.setIfPresent(input::getCreated, builder::setCreatedDate, ProtobufModelUtils::toProtobufTimestamp);

        builder.addAllAuthorizedLoginMethods(input.getAuthorizedLoginMethods().stream()
                .map(loginMethod -> EnumMappers.AUTHENTICATION_METHOD_TO_GRPC
                        .getOrDefault(loginMethod, AuthenticationMethod.AUTHENTICATION_METHOD_UNKNOWN))
                .collect(Collectors.toList()));

        builder.addAllAvailableLoginMethods(input.getAvailableLoginMethods().stream()
                .map(registerMethod -> EnumMappers.AUTHENTICATION_METHOD_TO_GRPC
                        .getOrDefault(registerMethod, AuthenticationMethod.AUTHENTICATION_METHOD_UNKNOWN))
                .collect(Collectors.toList()));

        return builder.build();
    }
}
