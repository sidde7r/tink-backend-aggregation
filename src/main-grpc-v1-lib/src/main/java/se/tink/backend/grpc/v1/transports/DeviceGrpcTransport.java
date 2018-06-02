package se.tink.backend.grpc.v1.transports;

import com.google.inject.Inject;
import io.grpc.stub.StreamObserver;
import java.util.List;
import se.tink.backend.auth.Authenticated;
import se.tink.backend.auth.AuthenticationContext;
import se.tink.backend.core.Device;
import se.tink.backend.core.DeviceConfiguration;
import se.tink.backend.grpc.v1.converter.devices.CoreDeviceToGrpcDevice;
import se.tink.backend.grpc.v1.converter.devices.DeviceConfigurationConverter;
import se.tink.backend.grpc.v1.converter.devices.GetDeviceConfigurationRequestConverter;
import se.tink.backend.grpc.v1.converter.devices.OriginRequestConverter;
import se.tink.backend.grpc.v1.interceptors.AuthenticationInterceptor;
import se.tink.backend.main.controllers.DeviceServiceController;
import se.tink.backend.rpc.RegisterUserPushTokenCommand;
import se.tink.grpc.v1.rpc.DeleteDeviceRequest;
import se.tink.grpc.v1.rpc.DeleteDeviceResponse;
import se.tink.grpc.v1.rpc.DeviceConfigurationResponse;
import se.tink.grpc.v1.rpc.GetDeviceConfigurationRequest;
import se.tink.grpc.v1.rpc.ListDevicesRequest;
import se.tink.grpc.v1.rpc.ListDevicesResponse;
import se.tink.grpc.v1.rpc.RegisterPushNotificationTokenRequest;
import se.tink.grpc.v1.rpc.RegisterPushNotificationTokenResponse;
import se.tink.grpc.v1.rpc.SetOriginRequest;
import se.tink.grpc.v1.rpc.SetOriginResponse;
import se.tink.grpc.v1.services.DeviceServiceGrpc;

public class DeviceGrpcTransport extends DeviceServiceGrpc.DeviceServiceImplBase {
    private final DeviceServiceController deviceServiceController;

    @Inject
    public DeviceGrpcTransport(DeviceServiceController deviceServiceController) {
        this.deviceServiceController = deviceServiceController;
    }

    @Override
    @Authenticated
    public void registerPushNotificationToken(RegisterPushNotificationTokenRequest request,
            StreamObserver<RegisterPushNotificationTokenResponse> responseObserver) {
        AuthenticationContext authenticationContext = AuthenticationInterceptor.CONTEXT.get();

        RegisterUserPushTokenCommand command = RegisterUserPushTokenCommand.builder()
                .withUserId(authenticationContext.getUser().getId())
                .withUserAgent(authenticationContext.getUserAgent().orElse(null))
                .withNotificationToken(request.getNotificationToken())
                .withNotificationPublicKey(request.getNotificationPublicKey())
                .withDeviceId(request.getDeviceId())
                .build();

        Device device = deviceServiceController.registerUserPushToken(command);

        responseObserver.onNext(RegisterPushNotificationTokenResponse.newBuilder()
                .setDevice(new CoreDeviceToGrpcDevice().convertFrom(device))
                .build());
        responseObserver.onCompleted();
    }

    @Override
    @Authenticated(required = false)
    public void getDeviceConfiguration(GetDeviceConfigurationRequest request,
            StreamObserver<DeviceConfigurationResponse> streamObserver) {
        GetDeviceConfigurationRequestConverter requestConverter = new GetDeviceConfigurationRequestConverter();
        DeviceConfiguration configuration = deviceServiceController.getConfiguration(requestConverter.convertFrom(request));
        DeviceConfigurationConverter converter = new DeviceConfigurationConverter();
        streamObserver.onNext(converter.convertFrom(configuration));
        streamObserver.onCompleted();
    }

    @Override
    @Authenticated(required = false)
    public void setOrigin(SetOriginRequest request, StreamObserver<SetOriginResponse> streamObserver) {

        OriginRequestConverter requestConverter = new OriginRequestConverter();
        deviceServiceController.setOrigin(requestConverter.convertFrom(request));

        streamObserver.onNext(SetOriginResponse.getDefaultInstance());
        streamObserver.onCompleted();

    }

    @Override
    @Authenticated
    public void listDevices(ListDevicesRequest listDevicesRequest, StreamObserver<ListDevicesResponse> streamObserver) {
        AuthenticationContext authenticationContext = AuthenticationInterceptor.CONTEXT.get();
        CoreDeviceToGrpcDevice converter = new CoreDeviceToGrpcDevice();
        List<se.tink.grpc.v1.models.Device> devices = converter
                .convertFrom(deviceServiceController.listDevices(authenticationContext.getUser().getId()));
        streamObserver.onNext(ListDevicesResponse.newBuilder().addAllDevices(devices).build());
        streamObserver.onCompleted();
    }

    @Override
    @Authenticated
    public void deleteDevice(DeleteDeviceRequest deleteDeviceRequest,
            StreamObserver<DeleteDeviceResponse> streamObserver) {
        AuthenticationContext authenticationContext = AuthenticationInterceptor.CONTEXT.get();
        deviceServiceController.deleteDevice(authenticationContext.getUser().getId(), deleteDeviceRequest.getDeviceId());
        streamObserver.onNext(DeleteDeviceResponse.getDefaultInstance());
        streamObserver.onCompleted();
    }
}
