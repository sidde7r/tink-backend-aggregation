package se.tink.backend.aggregation.rpc;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import se.tink.libraries.credentials.service.RefreshableItem;

public class RefreshableItemTest {

    private static final Ordering<RefreshableItem> REFRESHABLE_ITEM_ORDERING =
            Ordering.explicit(
                    ImmutableList.of(
                            RefreshableItem.CHECKING_ACCOUNTS,
                            RefreshableItem.SAVING_ACCOUNTS,
                            RefreshableItem.CREDITCARD_ACCOUNTS,
                            RefreshableItem.LOAN_ACCOUNTS,
                            RefreshableItem.INVESTMENT_ACCOUNTS,
                            RefreshableItem.CHECKING_TRANSACTIONS,
                            RefreshableItem.SAVING_TRANSACTIONS,
                            RefreshableItem.CREDITCARD_TRANSACTIONS,
                            RefreshableItem.LOAN_TRANSACTIONS,
                            RefreshableItem.INVESTMENT_TRANSACTIONS,
                            RefreshableItem.EINVOICES,
                            RefreshableItem.TRANSFER_DESTINATIONS,
                            RefreshableItem.ACCOUNTS,
                            RefreshableItem.TRANSACTIONAL_ACCOUNTS_AND_TRANSACTIONS,
                            RefreshableItem.IDENTITY));

    @Test
    public void testOrderingAll() {
        List<RefreshableItem> unsorted = Lists.newArrayList(RefreshableItem.values());

        List<RefreshableItem> sorted = REFRESHABLE_ITEM_ORDERING.sortedCopy(unsorted);

        Assert.assertEquals(RefreshableItem.CHECKING_ACCOUNTS, sorted.get(0));
        Assert.assertEquals(RefreshableItem.SAVING_ACCOUNTS, sorted.get(1));
        Assert.assertEquals(RefreshableItem.CREDITCARD_ACCOUNTS, sorted.get(2));
        Assert.assertEquals(RefreshableItem.LOAN_ACCOUNTS, sorted.get(3));
        Assert.assertEquals(RefreshableItem.INVESTMENT_ACCOUNTS, sorted.get(4));
        Assert.assertEquals(RefreshableItem.CHECKING_TRANSACTIONS, sorted.get(5));
        Assert.assertEquals(RefreshableItem.SAVING_TRANSACTIONS, sorted.get(6));
        Assert.assertEquals(RefreshableItem.CREDITCARD_TRANSACTIONS, sorted.get(7));
        Assert.assertEquals(RefreshableItem.LOAN_TRANSACTIONS, sorted.get(8));
        Assert.assertEquals(RefreshableItem.INVESTMENT_TRANSACTIONS, sorted.get(9));
        Assert.assertEquals(RefreshableItem.EINVOICES, sorted.get(10));
        Assert.assertEquals(RefreshableItem.TRANSFER_DESTINATIONS, sorted.get(11));
        Assert.assertEquals(RefreshableItem.ACCOUNTS, sorted.get(12));
        Assert.assertEquals(
                RefreshableItem.TRANSACTIONAL_ACCOUNTS_AND_TRANSACTIONS, sorted.get(13));
        Assert.assertEquals(RefreshableItem.IDENTITY, sorted.get(14));
    }

    @Test
    public void testOrderingAccountsAndEinvoices() {
        List<RefreshableItem> unsorted =
                Lists.newArrayList(
                        RefreshableItem.CREDITCARD_TRANSACTIONS, RefreshableItem.EINVOICES);

        List<RefreshableItem> sorted = REFRESHABLE_ITEM_ORDERING.sortedCopy(unsorted);

        Assert.assertEquals(RefreshableItem.CREDITCARD_TRANSACTIONS, sorted.get(0));
        Assert.assertEquals(RefreshableItem.EINVOICES, sorted.get(1));
    }

    @Test
    public void testOrderingAccountsAndTransferDestinations() {
        List<RefreshableItem> unsorted =
                Lists.newArrayList(
                        RefreshableItem.TRANSFER_DESTINATIONS, RefreshableItem.SAVING_ACCOUNTS);

        List<RefreshableItem> sorted = REFRESHABLE_ITEM_ORDERING.sortedCopy(unsorted);

        Assert.assertEquals(RefreshableItem.SAVING_ACCOUNTS, sorted.get(0));
        Assert.assertEquals(RefreshableItem.TRANSFER_DESTINATIONS, sorted.get(1));
    }
}
