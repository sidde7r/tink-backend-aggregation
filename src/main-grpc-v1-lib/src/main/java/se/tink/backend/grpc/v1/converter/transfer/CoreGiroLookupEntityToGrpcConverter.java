package se.tink.backend.grpc.v1.converter.transfer;

import java.net.URI;
import se.tink.backend.grpc.v1.converter.Converter;
import se.tink.backend.grpc.v1.converter.ConverterUtils;
import se.tink.backend.grpc.v1.converter.provider.CoreImageUrlsToGrpcImagesConverter;
import se.tink.grpc.v1.models.GiroLookupEntity;

public class CoreGiroLookupEntityToGrpcConverter implements
        Converter<se.tink.backend.rpc.GiroLookupEntity, GiroLookupEntity> {
    private final CoreImageUrlsToGrpcImagesConverter imageUrlsConverter = new CoreImageUrlsToGrpcImagesConverter();
    @Override
    public GiroLookupEntity convertFrom(se.tink.backend.rpc.GiroLookupEntity input) {
        GiroLookupEntity.Builder builder = GiroLookupEntity.newBuilder();
        ConverterUtils.setIfPresent(input::getDisplayName, builder::setDisplayName);
        ConverterUtils.setIfPresent(input::getIdentifier, builder::setIdentifier, URI::toString);
        ConverterUtils.setIfPresent(input::getImages, builder::setImages, imageUrlsConverter::convertFrom);
        ConverterUtils.setIfPresent(input::getDisplayNumber, builder::setDisplayNumber);
        return builder.build();
    }
}
