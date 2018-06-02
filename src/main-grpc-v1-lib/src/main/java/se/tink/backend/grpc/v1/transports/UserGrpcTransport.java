package se.tink.backend.grpc.v1.transports;

import com.google.inject.Inject;
import io.grpc.stub.StreamObserver;
import se.tink.backend.auth.Authenticated;
import se.tink.backend.auth.AuthenticationContext;
import se.tink.backend.core.User;
import se.tink.backend.grpc.v1.converter.EnumMappers;
import se.tink.backend.grpc.v1.converter.user.CoreUserProfileToGrpcUserProfileConverter;
import se.tink.backend.grpc.v1.converter.user.DeleteUserRequestConverter;
import se.tink.backend.grpc.v1.interceptors.AuthenticationInterceptor;
import se.tink.backend.main.controllers.UserServiceController;
import se.tink.backend.rpc.RateAppCommand;
import se.tink.backend.rpc.UserProfileResponse;
import se.tink.grpc.v1.rpc.DeleteUserRequest;
import se.tink.grpc.v1.rpc.DeleteUserResponse;
import se.tink.grpc.v1.rpc.GetProfileRequest;
import se.tink.grpc.v1.rpc.GetProfileResponse;
import se.tink.grpc.v1.rpc.RateAppRequest;
import se.tink.grpc.v1.rpc.RateAppResponse;
import se.tink.grpc.v1.services.UserServiceGrpc;

public class UserGrpcTransport extends UserServiceGrpc.UserServiceImplBase {
    private final UserServiceController userServiceController;

    @Inject
    public UserGrpcTransport(UserServiceController userServiceController) {
        this.userServiceController = userServiceController;
    }

    @Override
    @Authenticated
    public void deleteUser(DeleteUserRequest deleteUserRequest, StreamObserver<DeleteUserResponse> streamObserver) {
        AuthenticationContext authenticationContext = AuthenticationInterceptor.CONTEXT.get();

        userServiceController.delete(authenticationContext.getUser(),
                new DeleteUserRequestConverter(authenticationContext.getMetadata()).convertFrom(deleteUserRequest));
        streamObserver.onNext(DeleteUserResponse.getDefaultInstance());
        streamObserver.onCompleted();
    }

    @Override
    @Authenticated
    public void getProfile(GetProfileRequest request, StreamObserver<GetProfileResponse> responseObserver) {
        User user = AuthenticationInterceptor.CONTEXT.get().getUser();

        UserProfileResponse userProfile = userServiceController.getUserProfile(user);

        responseObserver.onNext(GetProfileResponse.newBuilder()
                .setUserProfile(new CoreUserProfileToGrpcUserProfileConverter().convertFrom(userProfile))
                .build());
        responseObserver.onCompleted();
    }

    @Override
    @Authenticated
    public void rateApp(RateAppRequest request, StreamObserver<RateAppResponse> responseObserver) {
        User user = AuthenticationInterceptor.CONTEXT.get().getUser();
        userServiceController.rateApp(new RateAppCommand(user.getId(),
                EnumMappers.CORE_RATE_APP_STATUS_TO_GRPC_MAP.inverse().get(request.getStatus())));
        responseObserver.onNext(RateAppResponse.getDefaultInstance());
        responseObserver.onCompleted();
    }
}
