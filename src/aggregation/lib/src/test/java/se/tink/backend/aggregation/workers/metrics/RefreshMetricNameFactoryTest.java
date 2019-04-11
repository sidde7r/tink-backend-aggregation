package se.tink.backend.aggregation.workers.metrics;

import static org.junit.Assert.assertEquals;

import com.google.common.collect.Lists;
import org.junit.Test;
import se.tink.libraries.credentials.service.RefreshableItem;

public class RefreshMetricNameFactoryTest {

    @Test
    public void testOperationNames() {
        String name1 =
                RefreshMetricNameFactory.createOperationName(
                        Lists.newArrayList(
                                RefreshableItem.TRANSACTIONAL_ACCOUNTS_AND_TRANSACTIONS,
                                RefreshableItem.EINVOICES),
                        true);

        String name2 =
                RefreshMetricNameFactory.createOperationName(
                        Lists.newArrayList(
                                RefreshableItem.TRANSACTIONAL_ACCOUNTS_AND_TRANSACTIONS,
                                RefreshableItem.ACCOUNTS),
                        true);

        assertEquals("refresh-einvoices-transactions-manual", name1);
        assertEquals("refresh-accounts-transactions-manual", name2);
    }

    @Test
    public void testOperationNamesWithAllItems() {
        String name1 =
                RefreshMetricNameFactory.createOperationName(
                        Lists.newArrayList(RefreshableItem.values()), true);
        String name2 =
                RefreshMetricNameFactory.createOperationName(
                        Lists.newArrayList(RefreshableItem.values()), false);

        assertEquals("refresh-manual", name1);
        assertEquals("refresh-auto", name2);
    }
}
