package se.tink.backend.grpc.v1.converter.application;

import java.util.UUID;
import se.tink.backend.grpc.v1.converter.Converter;
import se.tink.backend.grpc.v1.converter.ConverterUtils;
import se.tink.backend.grpc.v1.converter.EnumMappers;
import se.tink.grpc.v1.models.ApplicationStatusKey;
import se.tink.grpc.v1.models.ApplicationSummary;
import se.tink.grpc.v1.models.ApplicationSummaryStatus;
import se.tink.grpc.v1.models.ApplicationType;
import se.tink.libraries.uuid.UUIDUtils;

public class ApplicationSummaryConverter implements Converter<se.tink.backend.core.ApplicationSummary, ApplicationSummary> {
    @Override
    public ApplicationSummary convertFrom(se.tink.backend.core.ApplicationSummary input) {
        ApplicationSummary.Builder builder = ApplicationSummary.newBuilder();

        ConverterUtils.setIfPresent(input::getDescription, builder::setDescription);
        ConverterUtils.setIfPresent(input::getImageUrl, builder::setImageUrl);
        ConverterUtils.setIfPresent(input::getProgress, builder::setProgress);
        ConverterUtils.setIfPresent(input::getProvider, builder::setProvider);

        ApplicationSummaryStatus.Builder statusBuilder = ApplicationSummaryStatus.newBuilder();
        ConverterUtils.setIfPresent(input::getStatusKey, statusBuilder::setKey, key ->
            EnumMappers.APPLICATION_STATUS_KEY_TO_GRPC.getOrDefault(key, ApplicationStatusKey.APPLICATION_STATUS_UNKNOWN)
        );
        ConverterUtils.setIfPresent(input::getStatusBody, statusBuilder::setBody);
        ConverterUtils.setIfPresent(input::getStatusPayload, statusBuilder::setPayload);
        ConverterUtils.setIfPresent(input::getStatusTitle, statusBuilder::setTitle);
        builder.setStatus(statusBuilder);
        ConverterUtils.setIfPresent(input::getTitle, builder::setTitle);
        ConverterUtils.setIfPresent(input::getType, builder::setType, type ->
            EnumMappers.APPLICATION_TYPE_TO_GRPC.getOrDefault(type, ApplicationType.APPLICATION_TYPE_UNKNOWN)
        );

        UUID uuid = UUIDUtils.fromString(input.getId());

        // Use UUIDs in the gRPC api
        if (uuid != null) {
            builder.setId(uuid.toString());
        }

        return builder.build();
    }
}
