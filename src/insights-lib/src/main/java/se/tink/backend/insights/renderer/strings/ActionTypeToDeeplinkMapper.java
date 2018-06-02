package se.tink.backend.insights.renderer.strings;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import se.tink.backend.insights.core.valueobjects.InsightActionType;

public class ActionTypeToDeeplinkMapper {

    private final static Map<InsightActionType, String> classToDeeplink = initializeMapping();

    public static String getDeeplinkByInsightActionType(InsightActionType InsightActionType) {
        if (classToDeeplink.containsKey(InsightActionType)) {
            return classToDeeplink.get(InsightActionType);
        }
        return null;
    }

    private static ImmutableMap<InsightActionType, String> initializeMapping() {
        ImmutableMap.Builder<InsightActionType, String> mapBuilder = new ImmutableMap.Builder<>();
        mapBuilder.put(InsightActionType.ACKNOWLEDGE, "insights/dismiss");
        mapBuilder.put(InsightActionType.SUGGEST_CATEGORY, "suggest/categories");
        mapBuilder.put(InsightActionType.SWITCH_MORTGAGE, "applications/create/switch-mortgage-provider");

        mapBuilder.put(InsightActionType.GOTO_PAYMENT, "payment/%s");
        mapBuilder.put(InsightActionType.GOTO_PAYMENT_NO_ID, "payment");
        mapBuilder.put(InsightActionType.GOTO_TRANSFER, "transfer/%s");
        mapBuilder.put(InsightActionType.GOTO_TRANSFER_NO_ID, "transfer");
        mapBuilder.put(InsightActionType.GOTO_EXPENSE, "");
        mapBuilder.put(InsightActionType.GOTO_INCOME, "");
        mapBuilder.put(InsightActionType.GOTO_CATEGORIZE, "categories/%s");
        mapBuilder.put(InsightActionType.GOTO_BUDGET, "follow/%s");
        mapBuilder.put(InsightActionType.GOTO_ID_KOLL, "fraud");

        mapBuilder.put(InsightActionType.OPEN_SAVINGS_ACCOUNT, "applications/create/open-savings-account");
        mapBuilder.put(InsightActionType.ADD_PROVIDER, "providers/%s/add");
        mapBuilder.put(InsightActionType.CREATE_SAVINGS_GOAL, "follow");
        mapBuilder.put(InsightActionType.CREATE_BUDGET, "follow");

        mapBuilder.put(InsightActionType.EDIT_BUDGET, "follow/%s");
        mapBuilder.put(InsightActionType.EDIT_CREDENTIALS, "credentials/%s/edit");
        mapBuilder.put(InsightActionType.EDIT_LOAN, "accounts/%s");
        mapBuilder.put(InsightActionType.EDIT_SAVINGS_GOAL, "follow/%s");

        return mapBuilder.build();
    }
}
