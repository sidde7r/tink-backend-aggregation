package se.tink.backend.aggregation.compliance.customer_restrictions;

import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.Map;
import se.tink.libraries.credentials.service.DataFetchingRestrictions;
import se.tink.libraries.credentials.service.RefreshableItem;

public class CustomerDataFetchingRestrictions {

    private static final Map<RefreshableItem, DataFetchingRestrictions>
            REFRESHABLE_ITEM_TO_DATA_FETCHING_RESTRICTIONS_MAP =
                    ImmutableMap.<RefreshableItem, DataFetchingRestrictions>builder()
                            .put(
                                    RefreshableItem.CHECKING_ACCOUNTS,
                                    DataFetchingRestrictions.RESTRICT_FETCHING_CHECKING_ACCOUNTS)
                            .put(
                                    RefreshableItem.CHECKING_TRANSACTIONS,
                                    DataFetchingRestrictions
                                            .RESTRICT_FETCHING_CHECKING_TRANSACTIONS)
                            .put(
                                    RefreshableItem.SAVING_ACCOUNTS,
                                    DataFetchingRestrictions.RESTRICT_FETCHING_SAVINGS_ACCOUNTS)
                            .put(
                                    RefreshableItem.SAVING_TRANSACTIONS,
                                    DataFetchingRestrictions.RESTRICT_FETCHING_SAVINGS_TRANSACTIONS)
                            .put(
                                    RefreshableItem.CREDITCARD_ACCOUNTS,
                                    DataFetchingRestrictions.RESTRICT_FETCHING_CREDIT_CARD_ACCOUNTS)
                            .put(
                                    RefreshableItem.CREDITCARD_TRANSACTIONS,
                                    DataFetchingRestrictions
                                            .RESTRICT_FETCHING_CREDIT_CARD_TRANSACTIONS)
                            .put(
                                    RefreshableItem.LOAN_ACCOUNTS,
                                    DataFetchingRestrictions.RESTRICT_FETCHING_LOAN_ACCOUNTS)
                            .put(
                                    RefreshableItem.LOAN_TRANSACTIONS,
                                    DataFetchingRestrictions.RESTRICT_FETCHING_LOAN_TRANSACTIONS)
                            .put(
                                    RefreshableItem.INVESTMENT_ACCOUNTS,
                                    DataFetchingRestrictions.RESTRICT_FETCHING_INVESTMENT_ACCOUNTS)
                            .put(
                                    RefreshableItem.INVESTMENT_TRANSACTIONS,
                                    DataFetchingRestrictions
                                            .RESTRICT_FETCHING_INVESTMENT_TRANSACTIONS)
                            .build();

    public boolean shouldBeRestricted(
            RefreshableItem refreshableItem, List<DataFetchingRestrictions> restrictions) {
        return restrictions.stream()
                .anyMatch(
                        r ->
                                r
                                        == REFRESHABLE_ITEM_TO_DATA_FETCHING_RESTRICTIONS_MAP.get(
                                                refreshableItem));
    }
}
