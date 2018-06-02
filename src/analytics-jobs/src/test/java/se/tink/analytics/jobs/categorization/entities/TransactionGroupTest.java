package se.tink.analytics.jobs.categorization.entities;

import org.junit.Assert;
import org.junit.Test;

public class TransactionGroupTest {

    @Test
    public void userFilterBelowThresholdShouldBeRemoved() throws Exception {
        TransactionGroup transactionGroup1 = new TransactionGroup("NL", "description", "code", "user1");

        transactionGroup1.filterByMinUsers(2);

        Assert.assertTrue(transactionGroup1.isEmpty());
    }

    @Test
    public void userFilterAboveThresholdShouldNotBeRemoved() throws Exception {
        TransactionGroup transactionGroup1 = new TransactionGroup("NL", "description", "code", "user1");
        TransactionGroup transactionGroup2 = new TransactionGroup("NL", "description", "code", "user2");

        transactionGroup1.merge(transactionGroup2);

        transactionGroup1.filterByMinUsers(1);

        Assert.assertFalse(transactionGroup1.isEmpty());
    }

    @Test
    public void occurrencesFilterBelowThresholdShouldBeRemoved() throws Exception {
        TransactionGroup transactionGroup1 = new TransactionGroup("NL", "description", "code", "user1");

        transactionGroup1.filterByMinOccurrences(2);

        Assert.assertTrue(transactionGroup1.isEmpty());
    }

    @Test
    public void occurrencesFilterBelowThresholdShouldNotBeRemoved() throws Exception {
        TransactionGroup transactionGroup1 = new TransactionGroup("NL", "description", "code", "user1");
        TransactionGroup transactionGroup2 = new TransactionGroup("NL", "description", "code", "user2");

        transactionGroup1.merge(transactionGroup2);

        transactionGroup1.filterByMinOccurrences(1);

        Assert.assertFalse(transactionGroup1.isEmpty());
    }

    @Test
    public void mergeSameCategoryCode() throws Exception {
        TransactionGroup transactionGroup1 = new TransactionGroup("NL", "description", "code", "user1");

        transactionGroup1.merge(new TransactionGroup("NL", "description", "code", "user2"));

        Assert.assertEquals(1, transactionGroup1.getCategoryCodes().size());
        Assert.assertTrue(transactionGroup1.getCategoryCodes().contains("code"));
    }

    @Test
    public void mergeDifferentCategoryCode() throws Exception {
        TransactionGroup transactionGroup1 = new TransactionGroup("NL", "description", "code1", "user1");

        transactionGroup1.merge(new TransactionGroup("NL", "description", "code2", "user2"));

        Assert.assertEquals(2, transactionGroup1.getCategoryCodes().size());
        Assert.assertTrue(transactionGroup1.getCategoryCodes().contains("code1"));
        Assert.assertTrue(transactionGroup1.getCategoryCodes().contains("code2"));
    }
}
