package se.tink.backend.grpc.v1.converter.follow;

import com.google.common.base.Strings;
import java.util.Map;
import se.tink.backend.core.follow.ExpensesFollowCriteria;
import se.tink.backend.core.follow.SavingsFollowCriteria;
import se.tink.backend.core.follow.SearchFollowCriteria;
import se.tink.backend.grpc.v1.utils.ProtobufModelUtils;
import se.tink.libraries.serialization.utils.SerializationUtils;
import se.tink.backend.core.follow.FollowCriteria;
import se.tink.backend.grpc.v1.converter.Converter;
import se.tink.backend.grpc.v1.converter.ConverterUtils;
import se.tink.grpc.v1.models.FollowItem;
import se.tink.grpc.v1.models.Period;

public class CoreFollowItemToGrpcFollowItemConverter
        implements Converter<se.tink.backend.core.follow.FollowItem, FollowItem> {
    private final CoreExpensesFollowCriteriaToGrpcExpensesFollowCriteriaConverter expensesFollowCriteriaConverter;
    private final CoreSearchFollowCriteriaToGrpcSearchFollowCriteriaConverter searchFollowCriteriaConverter = new CoreSearchFollowCriteriaToGrpcSearchFollowCriteriaConverter();
    private final CoreSavingsFollowCriteriaToGrpcSavingsFollowCriteriaConverter savingsFollowCriteriaConverter = new CoreSavingsFollowCriteriaToGrpcSavingsFollowCriteriaConverter();
    private final CoreFollowDataToGrpcFollowDataConverter followDataConverter;

    public CoreFollowItemToGrpcFollowItemConverter(Map<String, Period> periodsByName, String transactionCurrencyCode,
            Map<String, String> categoryCodeById) {
        expensesFollowCriteriaConverter = new CoreExpensesFollowCriteriaToGrpcExpensesFollowCriteriaConverter(
                categoryCodeById);
        followDataConverter = new CoreFollowDataToGrpcFollowDataConverter(periodsByName, transactionCurrencyCode,
                categoryCodeById);
    }

    @Override
    public FollowItem convertFrom(se.tink.backend.core.follow.FollowItem input) {
        FollowItem.Builder builder = FollowItem.newBuilder();
        if (!Strings.isNullOrEmpty(input.getCriteria())) {
            switch (input.getType()) {
            case SEARCH:
                SearchFollowCriteria searchFollowCriteria = deserialize(input.getCriteria(),
                        se.tink.backend.core.follow.SearchFollowCriteria.class);
                builder.setSearchCriteria(searchFollowCriteriaConverter.convertFrom(searchFollowCriteria));
                break;
            case SAVINGS:
                SavingsFollowCriteria savingsFollowCriteria = deserialize(input.getCriteria(),
                        se.tink.backend.core.follow.SavingsFollowCriteria.class);
                builder.setSavingCriteria(savingsFollowCriteriaConverter.convertFrom(savingsFollowCriteria));
                break;
            case EXPENSES:
                ExpensesFollowCriteria expensesFollowCriteria = deserialize(input.getCriteria(),
                        se.tink.backend.core.follow.ExpensesFollowCriteria.class);
                builder.setExpensesCriteria(expensesFollowCriteriaConverter.convertFrom(expensesFollowCriteria));
                break;
            }
        }

        ConverterUtils.setIfPresent(input::getData, builder::setData, followDataConverter::convertFrom);
        ConverterUtils.setIfPresent(input::getId, builder::setId);
        ConverterUtils.setIfPresent(input::getName, builder::setName);
        ConverterUtils.setIfPresent(input::getCreated, builder::setCreatedDate, ProtobufModelUtils::toProtobufTimestamp);
        return builder.build();
    }

    private <T extends FollowCriteria> T deserialize(String criteria, Class<T> clazz) {
        return SerializationUtils.deserializeFromString(criteria, clazz);
    }
}
