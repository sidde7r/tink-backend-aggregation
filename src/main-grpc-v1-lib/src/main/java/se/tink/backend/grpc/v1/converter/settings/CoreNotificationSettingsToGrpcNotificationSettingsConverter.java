package se.tink.backend.grpc.v1.converter.settings;

import java.util.stream.Collectors;
import se.tink.backend.core.notifications.NotificationGroupSettings;
import se.tink.backend.grpc.v1.converter.Converter;
import se.tink.backend.grpc.v1.converter.ConverterUtils;
import se.tink.grpc.v1.models.NotificationGroup;
import se.tink.grpc.v1.models.NotificationSettings;
import se.tink.grpc.v1.models.NotificationType;

public class CoreNotificationSettingsToGrpcNotificationSettingsConverter
        implements Converter<NotificationGroupSettings, NotificationSettings> {

    @Override
    public NotificationSettings convertFrom(NotificationGroupSettings input) {

        NotificationSettings.Builder builder = NotificationSettings.newBuilder();
        builder.addAllGroups(input.getGroups()
                .stream()
                .map(CoreNotificationSettingsToGrpcNotificationSettingsConverter::convert)
                .collect(Collectors.toList()));

        return builder.build();
    }

    private static NotificationType convert(se.tink.backend.core.notifications.NotificationType input) {
        NotificationType.Builder builder = NotificationType.newBuilder();
        ConverterUtils.setIfPresent(input::getKey, builder::setKey);
        ConverterUtils.setIfPresent(input::getTitle, builder::setTitle);
        ConverterUtils.setIfPresent(input::isEnabled, builder::setEnabled);

        return builder.build();
    }

    private static NotificationGroup convert(se.tink.backend.core.notifications.NotificationGroup input) {
        NotificationGroup.Builder builder = NotificationGroup.newBuilder();

        ConverterUtils.setIfPresent(input::getTitle, builder::setTitle);
        builder.addAllNotificationTypes(input.getTypes()
                .stream()
                .map(CoreNotificationSettingsToGrpcNotificationSettingsConverter::convert)
                .collect(Collectors.toList()));

        return builder.build();
    }
}
