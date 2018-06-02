package se.tink.backend.grpc.v1.converter.follow;

import java.util.Map;
import se.tink.backend.grpc.v1.converter.Converter;
import se.tink.backend.grpc.v1.converter.ConverterUtils;
import se.tink.backend.grpc.v1.utils.NumberUtils;
import se.tink.grpc.v1.models.ExpensesFollowCriteria;

public class CoreExpensesFollowCriteriaToGrpcExpensesFollowCriteriaConverter
        implements Converter<se.tink.backend.core.follow.ExpensesFollowCriteria, ExpensesFollowCriteria> {
    private final Map<String, String> categoryCodeById;

    public CoreExpensesFollowCriteriaToGrpcExpensesFollowCriteriaConverter(
            Map<String, String> categoryCodeById) {
        this.categoryCodeById = categoryCodeById;
    }

    @Override
    public ExpensesFollowCriteria convertFrom(se.tink.backend.core.follow.ExpensesFollowCriteria input) {
        ExpensesFollowCriteria.Builder builder = ExpensesFollowCriteria.newBuilder();
        ConverterUtils.setIfPresent(input::getTargetAmount, builder::setTargetAmount, NumberUtils::toExactNumber);
        ConverterUtils.mapList(input::getCategoryIds, builder::addAllCategoryCodes, categoryCodeById::get);
        return builder.build();
    }
}
