package se.tink.backend.grpc.v1.transports;

import com.google.inject.Inject;
import io.grpc.stub.StreamObserver;
import se.tink.backend.auth.Authenticated;
import se.tink.backend.auth.AuthenticationContext;
import se.tink.backend.core.I18nSettings;
import se.tink.backend.core.User;
import se.tink.backend.grpc.v1.converter.settings.CoreI18nSettingsToGrpcConverter;
import se.tink.backend.grpc.v1.converter.settings.CoreNotificationSettingsToGrpcNotificationSettingsConverter;
import se.tink.backend.grpc.v1.converter.settings.CorePeriodSettingsToGrpcConverter;
import se.tink.backend.grpc.v1.converter.settings.NotificationSettingsRequestConverter;
import se.tink.backend.grpc.v1.converter.settings.PeriodSettingsRequestConverter;
import se.tink.backend.grpc.v1.interceptors.AuthenticationInterceptor;
import se.tink.backend.main.controllers.SettingsServiceController;
import se.tink.backend.rpc.UpdateI18nSettingsCommand;
import se.tink.backend.rpc.UpdateNotificationSettingsCommand;
import se.tink.backend.rpc.UpdatePeriodSettingsCommand;
import se.tink.grpc.v1.models.NotificationSettings;
import se.tink.grpc.v1.rpc.NotificationSettingsRequest;
import se.tink.grpc.v1.rpc.NotificationSettingsResponse;
import se.tink.grpc.v1.rpc.PeriodSettingsRequest;
import se.tink.grpc.v1.rpc.PeriodSettingsResponse;
import se.tink.grpc.v1.rpc.UpdateI18NSettingsRequest;
import se.tink.grpc.v1.rpc.UpdateI18NSettingsResponse;
import se.tink.grpc.v1.rpc.UpdateNotificationSettingsRequest;
import se.tink.grpc.v1.rpc.UpdatePeriodSettingsRequest;
import se.tink.grpc.v1.services.SettingsServiceGrpc;

public class SettingsGrpcTransport extends SettingsServiceGrpc.SettingsServiceImplBase {
    private final SettingsServiceController settingsServiceController;

    @Inject
    public SettingsGrpcTransport(SettingsServiceController settingsServiceController) {
        this.settingsServiceController = settingsServiceController;
    }

    @Override
    @Authenticated
    public void getNotificationSettings(NotificationSettingsRequest request, StreamObserver<NotificationSettingsResponse> streamObserver) {
        AuthenticationContext authenticationContext = AuthenticationInterceptor.CONTEXT.get();
        User user = authenticationContext.getUser();

        NotificationSettings notificationSettings = new CoreNotificationSettingsToGrpcNotificationSettingsConverter()
                .convertFrom(settingsServiceController.getNotificationSettings(user));

        streamObserver.onNext(NotificationSettingsResponse.newBuilder().setNotificationSettings(notificationSettings).build());
        streamObserver.onCompleted();
    }

    @Override
    @Authenticated
    public void updateNotificationSettings(UpdateNotificationSettingsRequest notificationSettingsRequest,
            StreamObserver<NotificationSettingsResponse> streamObserver) {
        AuthenticationContext authenticationContext = AuthenticationInterceptor.CONTEXT.get();
        User user = authenticationContext.getUser();

        UpdateNotificationSettingsCommand updateNotificationSettings =
                new NotificationSettingsRequestConverter()
                        .convertFrom(notificationSettingsRequest);

        streamObserver.onNext(NotificationSettingsResponse.newBuilder().setNotificationSettings(
                new CoreNotificationSettingsToGrpcNotificationSettingsConverter().convertFrom(
                        settingsServiceController.updateNotificationSettings(user, updateNotificationSettings)))
                        .build()
        );

        streamObserver.onCompleted();
    }

    @Override
    @Authenticated
    public void getPeriodSettings(PeriodSettingsRequest request, StreamObserver<PeriodSettingsResponse> streamObserver) {
        AuthenticationContext authenticationContext = AuthenticationInterceptor.CONTEXT.get();
        User user = authenticationContext.getUser();

        streamObserver.onNext(PeriodSettingsResponse.newBuilder().setPeriodSettings(
                new CorePeriodSettingsToGrpcConverter()
                        .convertFrom(settingsServiceController.getPeriodSettings(user))).build());
        streamObserver.onCompleted();
    }

    @Override
    @Authenticated
    public void updatePeriodSettings(UpdatePeriodSettingsRequest updatePeriodSettingsRequest,
            StreamObserver<PeriodSettingsResponse> streamObserver) {
        AuthenticationContext authenticationContext = AuthenticationInterceptor.CONTEXT.get();
        User user = authenticationContext.getUser();

        UpdatePeriodSettingsCommand updatePeriodSettings = new PeriodSettingsRequestConverter()
                .convertFrom(updatePeriodSettingsRequest);
        streamObserver.onNext(PeriodSettingsResponse.newBuilder().setPeriodSettings(
                new CorePeriodSettingsToGrpcConverter()
                        .convertFrom(settingsServiceController.updatePeriodSettings(user, updatePeriodSettings)))
                .build());
        streamObserver.onCompleted();
    }

    @Override
    @Authenticated
    public void updateI18NSettings(UpdateI18NSettingsRequest request,
            StreamObserver<UpdateI18NSettingsResponse> responseObserver) {
        AuthenticationContext authenticationContext = AuthenticationInterceptor.CONTEXT.get();
        User user = authenticationContext.getUser();
        I18nSettings i18nSettings = settingsServiceController
                .updateI18nSettings(user, new UpdateI18nSettingsCommand(request.getLocaleCode().getValue()));
        responseObserver.onNext(UpdateI18NSettingsResponse.newBuilder()
                .setI18NSettings(new CoreI18nSettingsToGrpcConverter().convertFrom(i18nSettings))
                .build());
        responseObserver.onCompleted();
    }
}
