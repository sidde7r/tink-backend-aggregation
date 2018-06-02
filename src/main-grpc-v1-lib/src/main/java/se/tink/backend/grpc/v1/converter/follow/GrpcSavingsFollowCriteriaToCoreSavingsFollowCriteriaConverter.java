package se.tink.backend.grpc.v1.converter.follow;

import se.tink.backend.core.follow.SavingsFollowCriteria;
import se.tink.backend.grpc.v1.converter.Converter;
import se.tink.backend.grpc.v1.converter.ConverterUtils;
import se.tink.backend.grpc.v1.utils.NumberUtils;

public class GrpcSavingsFollowCriteriaToCoreSavingsFollowCriteriaConverter
        implements Converter<se.tink.grpc.v1.models.SavingsFollowCriteria, SavingsFollowCriteria> {
    @Override
    public SavingsFollowCriteria convertFrom(se.tink.grpc.v1.models.SavingsFollowCriteria input) {
        SavingsFollowCriteria criteria = new SavingsFollowCriteria();
        ConverterUtils.setIfPresent(input::getTargetAmount, criteria::setTargetAmount, NumberUtils::toDouble);
        ConverterUtils.setIfPresent(input::getAccountIdsList, criteria::setAccountIds);
        ConverterUtils.setIfPresent(input::getPeriod, criteria::setTargetPeriod);
        return criteria;
    }
}
