package se.tink.backend.grpc.v1.converter.follow;

import java.util.Map;
import se.tink.backend.core.follow.ExpensesFollowCriteria;
import se.tink.backend.grpc.v1.converter.Converter;
import se.tink.backend.grpc.v1.converter.ConverterUtils;
import se.tink.backend.grpc.v1.utils.NumberUtils;

public class GrpcExpensesFollowCriteriaToCoreExpensesFollowCriteriaConverter
        implements Converter<se.tink.grpc.v1.models.ExpensesFollowCriteria, ExpensesFollowCriteria> {
    private final Map<String, String> categoryIdByCode;

    public GrpcExpensesFollowCriteriaToCoreExpensesFollowCriteriaConverter(
            Map<String, String> categoryIdByCode) {
        this.categoryIdByCode = categoryIdByCode;
    }

    @Override
    public ExpensesFollowCriteria convertFrom(se.tink.grpc.v1.models.ExpensesFollowCriteria input) {
        ExpensesFollowCriteria criteria = new ExpensesFollowCriteria();
        ConverterUtils.setIfPresent(input::getTargetAmount, criteria::setTargetAmount, NumberUtils::toDouble);
        ConverterUtils.mapList(input::getCategoryCodesList, criteria::setCategoryIds, categoryIdByCode::get);
        return criteria;
    }
}
