package se.tink.backend.grpc.v1.converter.follow;

import se.tink.backend.grpc.v1.converter.Converter;
import se.tink.backend.grpc.v1.converter.ConverterUtils;
import se.tink.backend.grpc.v1.utils.NumberUtils;
import se.tink.grpc.v1.models.SavingsFollowCriteria;

public class CoreSavingsFollowCriteriaToGrpcSavingsFollowCriteriaConverter
        implements Converter<se.tink.backend.core.follow.SavingsFollowCriteria, SavingsFollowCriteria> {
    @Override
    public SavingsFollowCriteria convertFrom(se.tink.backend.core.follow.SavingsFollowCriteria input) {
        SavingsFollowCriteria.Builder builder = SavingsFollowCriteria.newBuilder();
        ConverterUtils.setIfPresent(input::getTargetAmount, builder::setTargetAmount, NumberUtils::toExactNumber);
        ConverterUtils.setIfPresent(input::getAccountIds, builder::addAllAccountIds);
        ConverterUtils.setIfPresent(input::getTargetPeriod, builder::setPeriod);
        return builder.build();
    }
}
