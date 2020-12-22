package se.tink.backend.aggregation.workers.refresh.individual_refresh;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableSet;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.Test;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.libraries.credentials.service.RefreshInformationRequest;
import se.tink.libraries.credentials.service.RefreshableItem;

public class AccountClosureUtilTest {

    /*
     * Scenario:
     * - requestedAccountIds are present in the request
     * Expected result: should return unmodified requestedAccountIds
     */
    @Test
    public void shouldReturnRequestedAccountIdsIfPresent() {
        RefreshInformationRequest request = new RefreshInformationRequest();
        request.setRequestedAccountIds(ImmutableSet.of("acc1", "acc2"));
        List<Account> processedAccounts =
                Arrays.asList(
                        createAccount("acc1", AccountTypes.CHECKING),
                        createAccount("acc2", AccountTypes.SAVINGS));

        Set<String> accIds = AccountClosureUtil.getRequestedAccountIds(request, processedAccounts);
        assertThat(accIds).containsExactlyInAnyOrder("acc1", "acc2");
    }

    /*
     * Scenario:
     * - requestedAccountIds are not given
     * - Refreshable Items: CHECKING, SAVING, CREDIT_CARD
     * - two accounts (CHECKING and SAVING) have been aggregated in the past
     * - an additional account (CREDIT_CARD) has been returned by bank
     * Expected result: should return the all three accounts
     */
    @Test
    public void shouldREturnAllThreeAccounts() {
        RefreshInformationRequest request = new RefreshInformationRequest();
        request.setItemsToRefresh(
                ImmutableSet.of(
                        RefreshableItem.CHECKING_ACCOUNTS,
                        RefreshableItem.SAVING_ACCOUNTS,
                        RefreshableItem.CREDITCARD_ACCOUNTS));
        request.setAccounts(
                Arrays.asList(
                        createAccount("acc1", AccountTypes.CHECKING),
                        createAccount("acc2", AccountTypes.SAVINGS)));
        List<Account> processedAccounts =
                Arrays.asList(
                        createAccount("acc1", AccountTypes.CHECKING),
                        createAccount("acc2", AccountTypes.SAVINGS),
                        createAccount("acc3", AccountTypes.CREDIT_CARD));

        Set<String> accIds = AccountClosureUtil.getRequestedAccountIds(request, processedAccounts);
        assertThat(accIds).containsExactlyInAnyOrder("acc1", "acc2", "acc3");
    }

    /*
     * Scenario:
     * - requestedAccountIds are not given
     * - Refreshable Items: CHECKING, CREDIT_CARD
     * - two accounts (CHECKING and SAVING) have been aggregated in the past
     * - CHECKING account and an additional account (CREDIT_CARD) has been returned by bank
     * (SAVING one was not because of missing RefreshableItem)
     * Expected result: should return CHECKING and CREDIT_CARD account
     */
    @Test
    public void shouldNotReturnAccountNotRefreshedBecauseOfMissingRefreshableItem() {
        RefreshInformationRequest request = new RefreshInformationRequest();
        request.setItemsToRefresh(
                ImmutableSet.of(
                        RefreshableItem.CHECKING_ACCOUNTS, RefreshableItem.CREDITCARD_ACCOUNTS));
        request.setAccounts(
                Arrays.asList(
                        createAccount("acc1", AccountTypes.CHECKING),
                        createAccount("acc2", AccountTypes.SAVINGS)));
        List<Account> processedAccounts =
                Arrays.asList(
                        createAccount("acc1", AccountTypes.CHECKING),
                        createAccount("acc3", AccountTypes.CREDIT_CARD));

        Set<String> accIds = AccountClosureUtil.getRequestedAccountIds(request, processedAccounts);
        assertThat(accIds).containsExactlyInAnyOrder("acc1", "acc3");
    }

    /*
     * Scenario: if no accounts have been aggregated in the past then we can't expect that refresh will
     * return anything
     */
    @Test
    public void shouldReturnFalseIfNoAccountAggregatedBefore() {
        RefreshInformationRequest request = new RefreshInformationRequest();
        request.setAccounts(Collections.emptyList());
        request.setItemsToRefresh(
                Arrays.stream(RefreshableItem.values()).collect(Collectors.toSet()));
        boolean result = AccountClosureUtil.isRefreshExpectedToReturnAtLeastOneAccount(request);

        assertThat(result)
                .withFailMessage(
                        "If no accounts have been aggregated in the past we can't expect refresh will return anything")
                .isFalse();
    }

    /*
     * Scenario: CHECKING and SAVING accounts have been aggregated before but the ongoing refresh
     * has only RefreshableItem.CREDITCARD_ACCOUNTS and hence we can't expect that refresh will
     * return anything
     */
    @Test
    public void shouldReturnFalseIfRefreshableItemsDoNotMatchAccountsAggregatedBefore() {
        RefreshInformationRequest request = new RefreshInformationRequest();
        request.setAccounts(
                Arrays.asList(
                        createAccount("123", AccountTypes.CHECKING),
                        createAccount("256", AccountTypes.SAVINGS)));
        request.setItemsToRefresh(ImmutableSet.of(RefreshableItem.CREDITCARD_ACCOUNTS));
        boolean result = AccountClosureUtil.isRefreshExpectedToReturnAtLeastOneAccount(request);

        assertThat(result)
                .withFailMessage(
                        "If refreshable items do not match previously aggregated account we can't expect refresh will return anything")
                .isFalse();
    }

    /*
     * Scenario: CHECKING and SAVING accounts have been aggregated before and the ongoing refresh
     * has RefreshableItem.CHECKING_ACCOUNTS and hence we can expect that refresh will
     * return at least the checking account
     */
    @Test
    public void foo() {
        RefreshInformationRequest request = new RefreshInformationRequest();
        request.setAccounts(
                Arrays.asList(
                        createAccount("123", AccountTypes.CHECKING),
                        createAccount("256", AccountTypes.SAVINGS)));
        request.setItemsToRefresh(ImmutableSet.of(RefreshableItem.CHECKING_ACCOUNTS));
        boolean result = AccountClosureUtil.isRefreshExpectedToReturnAtLeastOneAccount(request);

        assertThat(result).withFailMessage("We expect CHECKING account to be refreshed").isTrue();
    }

    private Account createAccount(String id, AccountTypes accountType) {
        Account a = new Account();
        a.setId(id);
        a.setType(accountType);
        return a;
    }
}
