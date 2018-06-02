package se.tink.backend.grpc.v1.transports;

import com.google.inject.Inject;
import io.grpc.stub.StreamObserver;
import se.tink.backend.auth.Authenticated;
import se.tink.backend.auth.AuthenticationContext;
import se.tink.backend.core.User;
import se.tink.backend.grpc.v1.converter.tracking.CoreToTrackingSessionResponseConverter;
import se.tink.backend.grpc.v1.converter.tracking.TrackingRequestToCommandConverter;
import se.tink.backend.grpc.v1.interceptors.AuthenticationInterceptor;
import se.tink.backend.main.controllers.TrackingServiceController;
import se.tink.backend.main.controllers.UserTrackerController;
import se.tink.grpc.v1.rpc.GetTrackingConfigurationRequest;
import se.tink.grpc.v1.rpc.GetTrackingConfigurationResponse;
import se.tink.grpc.v1.rpc.TrackingRequest;
import se.tink.grpc.v1.rpc.TrackingResponse;
import se.tink.grpc.v1.rpc.TrackingSessionRequest;
import se.tink.grpc.v1.rpc.TrackingSessionResponse;
import se.tink.grpc.v1.services.TrackingServiceGrpc;

public class TrackingGrpcTransport extends TrackingServiceGrpc.TrackingServiceImplBase {
    private UserTrackerController userTrackerController;
    private TrackingServiceController trackingServiceController;

    @Inject
    public TrackingGrpcTransport(UserTrackerController userTrackerController,
            TrackingServiceController trackingServiceController) {
        this.userTrackerController = userTrackerController;
        this.trackingServiceController = trackingServiceController;

    }

    @Override
    @Authenticated
    public void getTrackingConfiguration(GetTrackingConfigurationRequest request,
            StreamObserver<GetTrackingConfigurationResponse> responseObserver) {
        AuthenticationContext authenticationContext = AuthenticationInterceptor.CONTEXT.get();
        User user = authenticationContext.getUser();
        responseObserver.onNext(GetTrackingConfigurationResponse.newBuilder()
                .setTrackingUserId(user.getId())
                .setTrackingUsername(user.getUsername()).build());
        responseObserver.onCompleted();
    }

    @Override
    @Authenticated(required = false)
    public void createSession(TrackingSessionRequest request, StreamObserver<TrackingSessionResponse> streamObserver) {
        streamObserver.onNext(new CoreToTrackingSessionResponseConverter()
                .convertFrom(trackingServiceController.createSession()));
        streamObserver.onCompleted();
    }

    @Override
    @Authenticated
    public void trackData(TrackingRequest request, StreamObserver<TrackingResponse> streamObserver) {
        AuthenticationContext authenticationContext = AuthenticationInterceptor.CONTEXT.get();
        User user = authenticationContext.getUser();
        userTrackerController
                .registerAdvertiserId(user.getId(), request.getAdvertisingId(), request.getLimitAdvertising(),
                        authenticationContext.getClientType().name());
        trackingServiceController
                .track(new TrackingRequestToCommandConverter(user, authenticationContext).convertFrom(request));
        streamObserver.onNext(TrackingResponse.getDefaultInstance());
        streamObserver.onCompleted();
    }

}
