package se.tink.backend.grpc.v1.converter.properties;

import se.tink.backend.grpc.v1.converter.Converter;
import se.tink.backend.grpc.v1.converter.ConverterUtils;
import se.tink.backend.grpc.v1.converter.EnumMappers;
import se.tink.grpc.v1.models.Property;

public class CorePropertyToGrpcPropertyConverter
        implements Converter<se.tink.backend.core.property.Property, Property> {

    @Override
    public Property convertFrom(se.tink.backend.core.property.Property input) {
        Property.Builder builder = Property.newBuilder();

        ConverterUtils.setIfPresent(input::getId, builder::setPropertyId);
        ConverterUtils.setIfPresent(input::getAddress, builder::setAddress);
        ConverterUtils.setIfPresent(input::getPostalCode, builder::setPostalCode);
        ConverterUtils.setIfPresent(input::getCity, builder::setCity);
        ConverterUtils.setIfPresent(input::getCommunity, builder::setCommunity);
        ConverterUtils.setIfPresent(input::getLatitude, builder::setLatitude);
        ConverterUtils.setIfPresent(input::getLongitude, builder::setLongitude);
        ConverterUtils.setIfPresent(input::getNumberOfRooms, builder::setNumberOfRooms);
        ConverterUtils.setIfPresent(input::getNumberOfSquareMeters, builder::setNumberOfSquareMeters);
        ConverterUtils.setIfPresent(input::getMostRecentValuation, builder::setValuation);
        ConverterUtils.setIfPresent(input::isRegisteredAddress, builder::setRegisteredAddress);
        ConverterUtils.setIfPresent(input::isUserModifiedLoanAccountIds, builder::setUserModifiedLoanAccountIds);

        ConverterUtils.setIfPresent(input::getType, builder::setType,
                type -> EnumMappers.PROPERTY_TYPE_TO_GRPC.getOrDefault(type, Property.Type.PROPERTY_TYPE_UNKNOWN));

        if (!input.getLoanAccountIds().isEmpty()) {
            builder.addAllLoanAccountIds(input.getLoanAccountIds());
        }

        return builder.build();
    }
}
