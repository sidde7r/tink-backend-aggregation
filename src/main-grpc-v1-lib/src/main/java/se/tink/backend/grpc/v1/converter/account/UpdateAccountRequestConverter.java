package se.tink.backend.grpc.v1.converter.account;

import com.google.protobuf.BoolValue;
import com.google.protobuf.StringValue;
import se.tink.backend.grpc.v1.converter.Converter;
import se.tink.backend.grpc.v1.converter.ConverterUtils;
import se.tink.backend.grpc.v1.converter.EnumMappers;
import se.tink.backend.grpc.v1.utils.NumberUtils;
import se.tink.backend.rpc.UpdateAccountRequest;

public class UpdateAccountRequestConverter
        implements Converter<se.tink.grpc.v1.rpc.UpdateAccountRequest, UpdateAccountRequest> {
    @Override
    public UpdateAccountRequest convertFrom(se.tink.grpc.v1.rpc.UpdateAccountRequest input) {
        UpdateAccountRequest updateRequest = new UpdateAccountRequest();
        ConverterUtils.setIfPresent(input::hasName, input::getName, updateRequest::setName, StringValue::getValue);
        ConverterUtils.setIfPresent(input::getType, updateRequest::setType,
                type -> EnumMappers.CORE_ACCOUNT_TYPE_TO_GRPC_MAP.inverse().get(type));
        ConverterUtils
                .setIfPresent(input::hasFavored, input::getFavored, updateRequest::setFavored, BoolValue::getValue);
        ConverterUtils
                .setIfPresent(input::hasExcluded, input::getExcluded, updateRequest::setExcluded, BoolValue::getValue);
        ConverterUtils.setIfPresent(input::hasOwnership, input::getOwnership, updateRequest::setOwnership,
                NumberUtils::toDouble);

        return updateRequest;
    }
}
