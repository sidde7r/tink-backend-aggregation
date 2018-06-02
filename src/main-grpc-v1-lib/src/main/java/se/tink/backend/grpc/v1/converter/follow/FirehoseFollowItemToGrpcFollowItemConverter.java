package se.tink.backend.grpc.v1.converter.follow;

import java.util.List;
import java.util.Map;
import se.tink.backend.core.follow.FollowCriteria;
import se.tink.backend.grpc.v1.converter.ConverterUtils;
import se.tink.backend.grpc.v1.utils.ProtobufModelUtils;
import se.tink.grpc.v1.models.FollowItem;
import se.tink.grpc.v1.models.FollowItems;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class FirehoseFollowItemToGrpcFollowItemConverter {
    private final CoreExpensesFollowCriteriaToGrpcExpensesFollowCriteriaConverter expensesFollowCriteriaConverter;
    private final CoreSearchFollowCriteriaToGrpcSearchFollowCriteriaConverter searchFollowCriteriaConverter = new CoreSearchFollowCriteriaToGrpcSearchFollowCriteriaConverter();
    private final CoreSavingsFollowCriteriaToGrpcSavingsFollowCriteriaConverter savingsFollowCriteriaConverter = new CoreSavingsFollowCriteriaToGrpcSavingsFollowCriteriaConverter();

    public FirehoseFollowItemToGrpcFollowItemConverter(Map<String, String> categoryCodeById) {
        expensesFollowCriteriaConverter = new CoreExpensesFollowCriteriaToGrpcExpensesFollowCriteriaConverter(
                categoryCodeById);
    }

    public FollowItems convertFrom(List<se.tink.backend.firehose.v1.models.FollowItem> input) {
        FollowItems.Builder followItemsBuilder = FollowItems.newBuilder();

        for (se.tink.backend.firehose.v1.models.FollowItem followItem : input) {
            FollowItem.Builder builder = FollowItem.newBuilder();
            switch (followItem.getType()) {
            case TYPE_SEARCH:
                ConverterUtils.setIfPresent(followItem::getCriteria, builder::setSearchCriteria,
                        criteria -> searchFollowCriteriaConverter.convertFrom(
                                deserialize(criteria, se.tink.backend.core.follow.SearchFollowCriteria.class)));
                break;
            case TYPE_SAVINGS:
                ConverterUtils.setIfPresent(followItem::getCriteria, builder::setSavingCriteria,
                        criteria -> savingsFollowCriteriaConverter.convertFrom(
                                deserialize(criteria, se.tink.backend.core.follow.SavingsFollowCriteria.class)));
                break;
            case TYPE_EXPENSES:
                ConverterUtils.setIfPresent(followItem::getCriteria, builder::setExpensesCriteria,
                        criteria -> expensesFollowCriteriaConverter.convertFrom(
                                deserialize(criteria, se.tink.backend.core.follow.ExpensesFollowCriteria.class)));
                break;
            default:
                break;
            }

            ConverterUtils.setIfPresent(followItem::getId, builder::setId);
            ConverterUtils.setIfPresent(followItem::getName, builder::setName);
            ConverterUtils.setIfPresent(followItem::getCreatedDate, builder::setCreatedDate,
                    ProtobufModelUtils::toProtobufTimestamp);
            followItemsBuilder.addFollowItem(builder);
        }
        return followItemsBuilder.build();
    }

    private <T extends FollowCriteria> T deserialize(String criteria, Class<T> clazz) {
        return SerializationUtils.deserializeFromString(criteria, clazz);
    }
}
