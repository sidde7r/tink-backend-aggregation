package se.tink.backend.aggregation.workers.refresh.individual_refresh;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableSet;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import org.junit.Test;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.libraries.credentials.service.RefreshInformationRequest;
import se.tink.libraries.credentials.service.RefreshableItem;

public class IndividualAccountRefreshUtilTest {

    /**
     * Scenario: - requestedAccountIds are present in the request Expected result: should return
     * unmodified requestedAccountIds
     */
    @Test
    public void shouldReturnRequestedAccountIdsIfPresent() {
        RefreshInformationRequest request = new RefreshInformationRequest();
        request.setRequestedAccountIds(ImmutableSet.of("acc1", "acc2"));
        List<Account> processedAccounts =
                Arrays.asList(
                        createAccount("acc1", AccountTypes.CHECKING),
                        createAccount("acc2", AccountTypes.SAVINGS));

        Set<String> accIds =
                IndividualAccountRefreshUtil.getRequestedAccountIds(request, processedAccounts);
        assertThat(accIds).containsExactlyInAnyOrder("acc1", "acc2");
    }

    /**
     * Scenario: - requestedAccountIds are not given - Refreshable Items: CHECKING, SAVING,
     * CREDIT_CARD - two accounts (CHECKING and SAVING) have been aggregated in the past - an
     * additional account (CREDIT_CARD) has been returned by bank Expected result: should return the
     * all three accounts
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

        Set<String> accIds =
                IndividualAccountRefreshUtil.getRequestedAccountIds(request, processedAccounts);
        assertThat(accIds).containsExactlyInAnyOrder("acc1", "acc2", "acc3");
    }

    /**
     * Scenario: - requestedAccountIds are not given - Refreshable Items: CHECKING, CREDIT_CARD -
     * two accounts (CHECKING and SAVING) have been aggregated in the past - CHECKING account and an
     * additional account (CREDIT_CARD) has been returned by bank (SAVING one was not because of
     * missing RefreshableItem) Expected result: should return CHECKING and CREDIT_CARD account
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

        Set<String> accIds =
                IndividualAccountRefreshUtil.getRequestedAccountIds(request, processedAccounts);
        assertThat(accIds).containsExactlyInAnyOrder("acc1", "acc3");
    }

    private Account createAccount(String id, AccountTypes accountType) {
        Account a = new Account();
        a.setId(id);
        a.setType(accountType);
        return a;
    }
}
