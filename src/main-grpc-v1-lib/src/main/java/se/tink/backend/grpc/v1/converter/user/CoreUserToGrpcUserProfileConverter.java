package se.tink.backend.grpc.v1.converter.user;

import se.tink.backend.core.User;
import se.tink.backend.grpc.v1.converter.Converter;
import se.tink.backend.grpc.v1.converter.ConverterUtils;
import se.tink.grpc.v1.models.UserProfile;

public class CoreUserToGrpcUserProfileConverter implements Converter<User, UserProfile> {
    @Override
    public UserProfile convertFrom(User user) {
        UserProfile.Builder builder = UserProfile.newBuilder();
        ConverterUtils.setIfPresent(user::getUsername, builder::setUsername);
        ConverterUtils.setIfPresent(user::getNationalId, builder::setNationalId);

        return builder.build();
    }
}
