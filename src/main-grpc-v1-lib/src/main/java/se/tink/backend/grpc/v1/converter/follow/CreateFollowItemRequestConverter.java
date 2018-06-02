package se.tink.backend.grpc.v1.converter.follow;

import java.util.Map;
import se.tink.backend.core.follow.FollowItem;
import se.tink.backend.core.follow.FollowTypes;
import se.tink.backend.grpc.v1.converter.Converter;
import se.tink.backend.grpc.v1.converter.ConverterUtils;
import se.tink.grpc.v1.rpc.CreateFollowItemRequest;

public class CreateFollowItemRequestConverter implements Converter<CreateFollowItemRequest, FollowItem> {
    private final GrpcExpensesFollowCriteriaToCoreExpensesFollowCriteriaConverter expensesFollowCriteriaConverter;
    private final GrpcSavingsFollowCriteriaToCoreSavingsFollowCriteriaConverter savingsFollowCriteriaConverter = new GrpcSavingsFollowCriteriaToCoreSavingsFollowCriteriaConverter();
    private final GrpcSearchFollowCriteriaToCoreSearchFollowCriteriaConverter searchFollowCriteriaConverter = new GrpcSearchFollowCriteriaToCoreSearchFollowCriteriaConverter();

    public CreateFollowItemRequestConverter(Map<String, String> categoryIdByCode) {
        expensesFollowCriteriaConverter = new GrpcExpensesFollowCriteriaToCoreExpensesFollowCriteriaConverter(
                categoryIdByCode);
    }

    @Override
    public FollowItem convertFrom(CreateFollowItemRequest input) {
        FollowItem followItem = new FollowItem();
        ConverterUtils.setIfPresent(input::getName, followItem::setName);
        if (input.hasSavingCriteria()) {
            followItem.setType(FollowTypes.SAVINGS);
            ConverterUtils.setIfPresent(input::getSavingCriteria, followItem::setFollowCriteria,
                    savingsFollowCriteriaConverter::convertFrom);
        } else if (input.hasExpensesCriteria()) {
            followItem.setType(FollowTypes.EXPENSES);
            ConverterUtils.setIfPresent(input::getExpensesCriteria, followItem::setFollowCriteria,
                    expensesFollowCriteriaConverter::convertFrom);
        } else if (input.hasSearchCriteria()) {
            followItem.setType(FollowTypes.SEARCH);
            ConverterUtils.setIfPresent(input::getSearchCriteria, followItem::setFollowCriteria,
                    searchFollowCriteriaConverter::convertFrom);
        }
        return followItem;
    }
}
