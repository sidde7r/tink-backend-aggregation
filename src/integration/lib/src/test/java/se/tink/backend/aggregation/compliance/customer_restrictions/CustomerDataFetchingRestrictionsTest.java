package se.tink.backend.aggregation.compliance.customer_restrictions;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Test;
import se.tink.libraries.credentials.service.DataFetchingRestrictions;
import se.tink.libraries.credentials.service.RefreshableItem;

public class CustomerDataFetchingRestrictionsTest {
    private CustomerDataFetchingRestrictions customerDataFetchingRestrictions =
            new CustomerDataFetchingRestrictions();

    @Test
    public void shouldRestrictChecking() {
        List<DataFetchingRestrictions> dataFetchingRestrictions =
                Arrays.asList(
                        DataFetchingRestrictions.RESTRICT_FETCHING_CHECKING_ACCOUNTS,
                        DataFetchingRestrictions.RESTRICT_FETCHING_CHECKING_TRANSACTIONS);
        List<RefreshableItem> restrictedItems =
                Arrays.asList(
                        RefreshableItem.CHECKING_ACCOUNTS, RefreshableItem.CHECKING_TRANSACTIONS);
        verifyRestrictions(dataFetchingRestrictions, restrictedItems);
    }

    @Test
    public void shouldRestrictCreditCardsAndSavings() {
        List<DataFetchingRestrictions> dataFetchingRestrictions =
                Arrays.asList(
                        DataFetchingRestrictions.RESTRICT_FETCHING_CREDIT_CARD_ACCOUNTS,
                                DataFetchingRestrictions.RESTRICT_FETCHING_CREDIT_CARD_TRANSACTIONS,
                        DataFetchingRestrictions.RESTRICT_FETCHING_SAVINGS_ACCOUNTS,
                                DataFetchingRestrictions.RESTRICT_FETCHING_SAVINGS_TRANSACTIONS);
        List<RefreshableItem> restrictedItems =
                Arrays.asList(
                        RefreshableItem.CREDITCARD_ACCOUNTS,
                        RefreshableItem.CREDITCARD_TRANSACTIONS,
                        RefreshableItem.SAVING_ACCOUNTS,
                        RefreshableItem.SAVING_TRANSACTIONS);
        verifyRestrictions(dataFetchingRestrictions, restrictedItems);
    }

    @Test
    public void shouldNotRestrictAnything() {
        List<DataFetchingRestrictions> dataFetchingRestrictions = Collections.emptyList();
        List<RefreshableItem> restrictedItems = Collections.emptyList();
        verifyRestrictions(dataFetchingRestrictions, restrictedItems);
    }

    private void verifyRestrictions(
            List<DataFetchingRestrictions> dataFetchingRestrictions,
            List<RefreshableItem> restrictedItems) {
        List<RefreshableItem> nonRestrictedItems =
                Arrays.stream(RefreshableItem.values())
                        .filter(ri -> !restrictedItems.contains(ri))
                        .collect(Collectors.toList());

        restrictedItems.forEach(
                restrictedItem -> {
                    assertThat(
                                    customerDataFetchingRestrictions.shouldBeRestricted(
                                            restrictedItem, dataFetchingRestrictions))
                            .isTrue();
                });

        nonRestrictedItems.forEach(
                nonRestrictedItem -> {
                    assertThat(
                                    customerDataFetchingRestrictions.shouldBeRestricted(
                                            nonRestrictedItem, dataFetchingRestrictions))
                            .isFalse();
                });
    }
}
