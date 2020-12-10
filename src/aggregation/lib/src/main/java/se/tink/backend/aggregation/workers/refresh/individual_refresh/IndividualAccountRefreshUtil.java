package se.tink.backend.aggregation.workers.refresh.individual_refresh;

import com.google.common.collect.ImmutableSet;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.collections4.CollectionUtils;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.libraries.credentials.service.RefreshInformationRequest;
import se.tink.libraries.credentials.service.RefreshableItem;

public class IndividualAccountRefreshUtil {

    /**
     * System closes accounts that have been aggregated previously but have not been returned with a
     * subsequent refresh. That can happen for example: - when just a subset of RefreshableItems is
     * specified - for Real Time Balance (e.g. individual account refresh) To ensure that System
     * won't close other accounts a proper information in ProcessAccountsRequest.requestedAccountIds
     * needs to be send to System
     *
     * <p>Note: Main (tink-backend) will reject all the granular refresh requests that are invalid,
     * for example: refresh for non-existing accounts only or refresh that can't refresh requested &
     * existing accounts due to missing Refreshable Item (example: refresh account123 (CHECKING) but
     * the only RefreshableItem given is SAVING).
     *
     * <p>Note: Main (tink-backend) can trim requestedAccountIds that come with the requested - if
     * any of the accountId given does not exist or is not refreshable (missing RefreshableItem)
     * then it will be removed. If after that process there are no requestedAccountIds left the
     * refresh request will be rejected. This means that all received requestedAccountIds in
     * AggregationService should be able to be refreshed (assuming that they still are available on
     * the bank side).
     */
    public static Set<String> getRequestedAccountIds(
            CredentialsRequest request, List<Account> processedAccounts) {
        if (!(request instanceof RefreshInformationRequest)) {
            return Collections.emptySet();
        }
        RefreshInformationRequest refreshRequest = (RefreshInformationRequest) request;
        Set<String> requestedAccountIds = refreshRequest.getRequestedAccountIds();
        if (!CollectionUtils.isEmpty(requestedAccountIds)) {
            return new HashSet<>(requestedAccountIds);
        }
        Set<AccountTypes> accountTypesToRefresh =
                mapRefreshableItemsToAccountTypes(refreshRequest.getItemsToRefresh());

        return Stream.of(refreshRequest.getAccounts(), processedAccounts)
                .flatMap(List::stream)
                .filter(a -> accountTypesToRefresh.contains(a.getType()))
                .map(Account::getId)
                .collect(Collectors.toSet());
    }

    private static Set<AccountTypes> mapRefreshableItemsToAccountTypes(
            Set<RefreshableItem> itemsToRefresh) {
        return itemsToRefresh.stream()
                .map(IndividualAccountRefreshUtil::mapRefreshableItemToAccountTypes)
                .flatMap(Set::stream)
                .collect(Collectors.toSet());
    }

    private static Set<AccountTypes> mapRefreshableItemToAccountTypes(RefreshableItem item) {
        switch (item) {
            case CHECKING_ACCOUNTS:
                return ImmutableSet.of(AccountTypes.CHECKING, AccountTypes.OTHER);
            case LOAN_ACCOUNTS:
                return ImmutableSet.of(AccountTypes.LOAN, AccountTypes.MORTGAGE);
            case SAVING_ACCOUNTS:
                return ImmutableSet.of(AccountTypes.SAVINGS);
            case CREDITCARD_ACCOUNTS:
                return ImmutableSet.of(AccountTypes.CREDIT_CARD);
            case INVESTMENT_ACCOUNTS:
                return ImmutableSet.of(AccountTypes.INVESTMENT, AccountTypes.PENSION);
            case ACCOUNTS:
                return ImmutableSet.copyOf(AccountTypes.values());
            default:
                return ImmutableSet.of();
        }
    }
}
