package se.tink.backend.grpc.v1.converter.provider;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import se.tink.backend.grpc.v1.converter.ConverterUtils;
import se.tink.backend.grpc.v1.converter.EnumMappers;
import se.tink.grpc.v1.models.Credential;
import se.tink.grpc.v1.models.Provider;
import se.tink.libraries.abnamro.utils.AbnAmroUtils;

public class CoreProviderToGrpcProviderConverter {
    private final CoreFieldsToGrpcProviderFieldsConverter providerFieldsConverter = new CoreFieldsToGrpcProviderFieldsConverter();
    private final CoreImageUrlsToGrpcImagesConverter imageUrlsConverter = new CoreImageUrlsToGrpcImagesConverter();

    public List<Provider> convertFrom(List<se.tink.backend.core.Provider> list) {
        return list.stream()

                /**
                 * Remove this mega-hack when Grip 4.0 are live. We need to have the ABN AMRO ICS Provider enabled for
                 * Grip 2.0 but disabled for Grip 4.0. Removing it temporary from Grpc converter since that only is
                 * used by Grip 4.0.
                 */
                .filter(c -> !Objects.equals(AbnAmroUtils.ABN_AMRO_ICS_PROVIDER_NAME, c.getName()))
                .map(this::convertFrom)
                .collect(Collectors.toList());
    }

    private Provider convertFrom(se.tink.backend.core.Provider input) {
        Provider.Builder builder = Provider.newBuilder();
        ConverterUtils.setIfPresent(input::getName, builder::setName);
        ConverterUtils.setIfPresent(input::getDisplayName, builder::setDisplayName);
        ConverterUtils.setIfPresent(input::getType, builder::setType,
                type -> EnumMappers.CORE_PROVIDER_TYPE_TO_GRPC_MAP.getOrDefault(type, Provider.Type.TYPE_UNKNOWN));
        ConverterUtils.setIfPresent(input::getStatus, builder::setStatus,
                type -> EnumMappers.CORE_PROVIDER_STATUS_TO_GRPC_MAP
                        .getOrDefault(type, Provider.Status.STATUS_UNKNOWN));
        ConverterUtils.setIfPresent(input::getCredentialsType, builder::setCredentialType,
                type -> EnumMappers.CORE_CREDENTIALS_TYPE_TO_GRPC_MAP.getOrDefault(type, Credential.Type.TYPE_UNKNOWN));
        ConverterUtils.setIfPresent(input::getPasswordHelpText, builder::setHelpText);
        ConverterUtils.setIfPresent(input::isPopular, builder::setPopular);
        ConverterUtils.setIfPresent(input::getFields, builder::addAllFields, providerFieldsConverter::convertFrom);
        ConverterUtils.setIfPresent(input::getGroupDisplayName, builder::setGroupDisplayName);
        ConverterUtils.setIfPresent(input::getImages, builder::setImages, imageUrlsConverter::convertFrom);
        ConverterUtils.setIfPresent(input::getDisplayDescription, builder::setDisplayDescription);
        ConverterUtils.setIfPresent(input::getCapabilities, builder::addAllCapabilities, capabilities ->
                capabilities.stream()
                        .map(capability -> EnumMappers.CORE_PROVIDER_CAPABILITY_TO_GRPC_MAP.get(capability))
                        .collect(Collectors.toList()));
        return builder.build();
    }
}
