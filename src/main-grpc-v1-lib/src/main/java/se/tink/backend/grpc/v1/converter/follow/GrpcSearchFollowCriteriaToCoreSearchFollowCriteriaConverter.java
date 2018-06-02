package se.tink.backend.grpc.v1.converter.follow;

import se.tink.backend.core.follow.SearchFollowCriteria;
import se.tink.backend.grpc.v1.converter.Converter;
import se.tink.backend.grpc.v1.converter.ConverterUtils;
import se.tink.backend.grpc.v1.utils.NumberUtils;

public class GrpcSearchFollowCriteriaToCoreSearchFollowCriteriaConverter
        implements Converter<se.tink.grpc.v1.models.SearchFollowCriteria, SearchFollowCriteria> {
    @Override
    public SearchFollowCriteria convertFrom(se.tink.grpc.v1.models.SearchFollowCriteria input) {
        SearchFollowCriteria criteria = new SearchFollowCriteria();
        ConverterUtils.setIfPresent(input::getTargetAmount, criteria::setTargetAmount, NumberUtils::toDouble);
        ConverterUtils.setIfPresent(input::getQuery, criteria::setQueryString);
        return criteria;
    }
}
