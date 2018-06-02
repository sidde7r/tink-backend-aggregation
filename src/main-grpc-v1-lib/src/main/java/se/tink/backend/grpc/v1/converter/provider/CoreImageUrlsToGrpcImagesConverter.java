package se.tink.backend.grpc.v1.converter.provider;

import se.tink.backend.core.ImageUrls;
import se.tink.backend.grpc.v1.converter.Converter;
import se.tink.backend.grpc.v1.converter.ConverterUtils;
import se.tink.grpc.v1.models.Images;

public class CoreImageUrlsToGrpcImagesConverter implements Converter<ImageUrls, Images> {
    @Override
    public Images convertFrom(ImageUrls input) {
        Images.Builder builder = Images.newBuilder();
        ConverterUtils.setIfPresent(input::getBanner, builder::setBannerUrl);
        ConverterUtils.setIfPresent(input::getIcon, builder::setIconUrl);
        return builder.build();
    }
}
