package se.tink.backend.grpc.v1.converter.follow;

import se.tink.backend.grpc.v1.converter.Converter;
import se.tink.backend.grpc.v1.converter.ConverterUtils;
import se.tink.backend.grpc.v1.utils.NumberUtils;
import se.tink.grpc.v1.models.SearchFollowCriteria;

public class CoreSearchFollowCriteriaToGrpcSearchFollowCriteriaConverter
        implements Converter<se.tink.backend.core.follow.SearchFollowCriteria, SearchFollowCriteria> {
    @Override
    public SearchFollowCriteria convertFrom(se.tink.backend.core.follow.SearchFollowCriteria input) {
        SearchFollowCriteria.Builder builder = SearchFollowCriteria.newBuilder();
        ConverterUtils.setIfPresent(input::getTargetAmount, builder::setTargetAmount, NumberUtils::toExactNumber);
        ConverterUtils.setIfPresent(input::getQueryString, builder::setQuery);
        return builder.build();
    }
}
