package se.tink.backend.system.workers.processor.deduplication.detector;

import java.util.UUID;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import se.tink.backend.core.Provider;
import se.tink.backend.core.Transaction;
import se.tink.backend.core.TransactionPayloadTypes;
import se.tink.libraries.date.DateUtils;
import se.tink.libraries.uuid.UUIDUtils;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class FuzzyTransactionMatcherTest {
    private static final String ACCOUNT_ID = UUIDUtils.toTinkUUID(UUID.randomUUID());

    private static class ProviderGroupName {
        private static final String SWEDBANK_SPARBANKERNA = "Swedbank och Sparbankerna";
        private static final String HANDELSBANKEN = "Handelsbanken";
        private static final String LANSFORSAKRINGAR = "Länsförsäkringar Bank";
    }

    private Provider provider;
    private Transaction t1;
    private Transaction t2;

    @Before
    public void setup() {
        provider = Mockito.mock(Provider.class);
        Mockito.when(provider.getGroupDisplayName()).thenReturn(ProviderGroupName.HANDELSBANKEN);

        t1 = TransactionCreator.create("ICA nära huddinge", 583, "2016-12-16", ACCOUNT_ID, true);
        t2 = t1.clone();
    }

    @Test
    public void createDefaultResultFromTransactionWithExternalId() {
        Transaction transaction = new Transaction();
        transaction.setPayload(TransactionPayloadTypes.EXTERNAL_ID, "external ID");
        new FuzzyTransactionMatcher.Result(transaction);
    }

    @Test
    public void ensureTransactionsWithMatchingExternalId_getMaxScore() {
        addExternalIdTo(t1, "1234567890");
        addExternalIdTo(t2, "1234567890");

        FuzzyTransactionMatcher.Result result = FuzzyTransactionMatcher.compare(t1, t2, provider, true);
        assertEquals(0, result.getDescriptionScore(), 0);
        assertEquals(0, result.getDateScore(), 0);
        assertEquals(0, result.getAmountScore(), 0);
        assertEquals(1, result.getScore(), 0);
    }

    @Test
    public void ensureTransactionsWithDifferentExternalIds_getMinScore() {
        addExternalIdTo(t1, "1234567890");
        addExternalIdTo(t2, "0987654321");

        FuzzyTransactionMatcher.Result result = FuzzyTransactionMatcher.compare(t1, t2, provider, true);
        assertEquals(0, result.getDescriptionScore(), 0);
        assertEquals(0, result.getDateScore(), 0);
        assertEquals(0, result.getAmountScore(), 0);
        assertEquals(0, result.getScore(), 0);
    }

    @Test
    public void ensureTransactionsThatBelongToDifferentAccounts_getMinScore() {
        t2.setAccountId(UUIDUtils.toTinkUUID(UUID.randomUUID()));

        FuzzyTransactionMatcher.Result result = FuzzyTransactionMatcher.compare(t1, t2, provider, true);
        assertEquals(0, result.getScore(), 0);
    }

    @Test
    public void ensureExactMatches_getMaxScore() {
        FuzzyTransactionMatcher.Result result = FuzzyTransactionMatcher.compare(t1, t2, provider, true);
        assertEquals(1, result.getScore(), 0);
    }

    @Test
    public void ensureDifferentDescriptions_doNotGetScoreOf_minOrMax() {
        t2.setOriginalDescription("BILTEMA haninge");

        FuzzyTransactionMatcher.Result result = FuzzyTransactionMatcher.compare(t1, t2, provider, true);

        double descriptionScore = result.getDescriptionScore();
        assertTrue(descriptionScore > 0 && descriptionScore < 1);
    }

    @Test
    public void ensureDifferentAmounts_doNotGetScoreOf_minOrMax() {
        t2.setPending(false);
        t2.setOriginalAmount(t1.getOriginalAmount() * 1.05);

        FuzzyTransactionMatcher.Result result = FuzzyTransactionMatcher.compare(t1, t2, provider, true);

        double amountScore = result.getAmountScore();
        assertTrue(amountScore > 0 && amountScore < 1);
    }

    @Test
    public void ensureDifferentDates_doNotGetScoreOf_minOrMax() {
        t2.setOriginalDate(DateUtils.parseDate("2016-12-18"));

        FuzzyTransactionMatcher.Result result = FuzzyTransactionMatcher.compare(t1, t2, provider, true);

        double dateScore = result.getDateScore();
        assertTrue(dateScore > 0 && dateScore < 1);
    }

    @Test
    public void ensurePastDates_getMinScore() {
        t2.setOriginalDate(DateUtils.parseDate("2016-12-15"));

        FuzzyTransactionMatcher.Result result = FuzzyTransactionMatcher.compare(t1, t2, provider, true);

        assertEquals(0, result.getDateScore(), 0);
    }

    @Test
    public void ensureCompletelyDifferentTransactions_getMinScore() {
        t2 = TransactionCreator.create("", 500, "2016-12-27", ACCOUNT_ID, true);

        FuzzyTransactionMatcher.Result result = FuzzyTransactionMatcher.compare(t1, t2, provider, true);
        assertEquals(0, result.getScore(), 0);
    }

    @Test
    public void ensureNonPendingExistingTransaction_getMinScore_whenMatchedWithPendingIncomingTransaction() {
        t1.setPending(false);

        FuzzyTransactionMatcher.Result result = FuzzyTransactionMatcher.compare(t1, t2, provider, true);
        assertEquals(0, result.getScore(), 0);
    }

    @Test
    public void ensureSimilarityScoreZeroWhen_amountScoreZero() {
        t2.setAmount(100.);
        t2.setOriginalAmount(t2.getAmount());

        FuzzyTransactionMatcher.Result result = FuzzyTransactionMatcher.compare(t1, t2, provider, true);
        assertEquals(0, result.getScore(), 0);
    }

    @Test
    public void ensureSimilarityScoreZeroWhen_dateScoreZero() {
        t2.setDate(DateUtils.addDays(t2.getDate(), 32));
        t2.setOriginalDate(t2.getDate());

        FuzzyTransactionMatcher.Result result = FuzzyTransactionMatcher.compare(t1, t2, provider, true);
        assertEquals(0, result.getScore(), 0);
    }

    @Test
    public void ensureSimilarityScoreZeroWhen_descriptionScoreZero() {
        t2.setOriginalDescription("");

        FuzzyTransactionMatcher.Result result = FuzzyTransactionMatcher.compare(t1, t2, provider, true);
        assertEquals(0, result.getScore(), 0);
    }

    @Test
    public void ensureSimilarityScoreZeroWhen_transactionSignsDiffer() {
        t1.setOriginalAmount(-132d);
        t2.setOriginalAmount(20d);

        FuzzyTransactionMatcher.Result result = FuzzyTransactionMatcher.compare(t1, t2, provider, true);
        assertEquals(0, result.getScore(), 0);
    }

    @Test
    public void ensureNonPendingExistingTransasction_getMinScore_whenDateNotExactMatch() {
        t1.setPending(false);
        t2.setDate(DateUtils.addDays(t2.getDate(), 1));
        t2.setOriginalDate(t2.getDate());
        t2.setPending(false);

        FuzzyTransactionMatcher.Result result = FuzzyTransactionMatcher.compare(t1, t2, provider, true);
        assertEquals(0, result.getScore(), 0);
    }

    @Test
    public void ensureNonPendingExistingTransaction_cannotBeMatchedWith_pendingIncomingTransaction() {
        t1.setPending(false);
        t2.setPending(true);

        FuzzyTransactionMatcher.Result result = FuzzyTransactionMatcher.compare(t1, t2, provider, true);
        assertEquals(0, result.getScore(), 0);
    }

    @Test
    public void ensureNonPendingTransactions_onlyMatches_whenTransactionsAreExactMatches() {
        t1.setPending(false);
        t2.setPending(false);

        // Description differ slightly ("a" instead of "ä" in "nära") = result(0)
        t2.setOriginalDescription("ICA nara huddinge");

        FuzzyTransactionMatcher.Result result = FuzzyTransactionMatcher.compare(t1, t2, provider, true);
        assertEquals(0, result.getScore(), 0);

        // Date differ slightly (1 day) = result(0)
        t2.setOriginalDescription(t1.getOriginalDescription());
        t2.setOriginalDate(DateUtils.addDays(t1.getOriginalDate(), 1));

        result = FuzzyTransactionMatcher.compare(t1, t2, provider, true);
        assertEquals(0, result.getScore(), 0);

        // Amount differ slightly (1 kr) = result(0)
        t2.setOriginalDate(t1.getOriginalDate());
        t2.setOriginalAmount(t1.getOriginalAmount() + 1);

        result = FuzzyTransactionMatcher.compare(t1, t2, provider, true);
        assertEquals(0, result.getScore(), 0);

        // Everything matches exactly = result(1)
        t2.setOriginalAmount(t1.getOriginalAmount());

        result = FuzzyTransactionMatcher.compare(t1, t2, provider, true);
        assertEquals(1, result.getScore(), 0);
    }

    @Test
    public void ensurePendingExistingTransaction_cannotMatch_pendingIncomingTransaction_whereDescriptionOrAmount_hasChanged() {
        // Description differ slightly ("a" instead of "ä" in "nära") = result(0)
        t2.setOriginalDescription("ICA nara huddinge");

        FuzzyTransactionMatcher.Result result = FuzzyTransactionMatcher.compare(t1, t2, provider, true);
        assertEquals(0, result.getScore(), 0);

        // Amount differ slightly (1 kr) = result(0)
        t2.setOriginalDescription(t1.getOriginalDescription());
        t2.setOriginalAmount(t1.getOriginalAmount() + 1);

        result = FuzzyTransactionMatcher.compare(t1, t2, provider, true);
        assertEquals(0, result.getScore(), 0);

        // Date differ slightly (5 days) = result(0.9746)
        t2.setOriginalAmount(t1.getOriginalAmount());
        t2.setOriginalDate(DateUtils.addDays(t1.getOriginalDate(), 5));

        result = FuzzyTransactionMatcher.compare(t1, t2, provider, true);
        assertEquals(0.9746, result.getScore(), 0.0001);
    }

    @Test
    public void ensureSettledTransactions_withPredefinedPendingDescription_onlyCompares_amountAndDate() {
        t2.setPending(false);

        // Description differs completely (predefined pending description) = result(1)
        t1.setOriginalDescription("Prel. KORTKÖP");

        FuzzyTransactionMatcher.Result result = FuzzyTransactionMatcher.compare(t1, t2, provider, true);
        assertEquals(1, result.getScore(), 0);

        // Amount differs slightly (16%) = result(0.9045)
        t2.setOriginalAmount(t1.getOriginalAmount() * 1.16);

        result = FuzzyTransactionMatcher.compare(t1, t2, provider, true);
        assertEquals(0.9045, result.getScore(), 0.0001);

        // Date differs slightly (5 days) = result(0.9746)
        t2.setOriginalAmount(t1.getOriginalAmount());
        t2.setOriginalDate(DateUtils.addDays(t1.getOriginalDate(), 5));

        result = FuzzyTransactionMatcher.compare(t1, t2, provider, true);
        assertEquals(0.9746, result.getScore(), 0.0001);
    }

    @Test
    public void ensureSettledTransactions_withoutPredefinedPendingDescription_compares_descriptionAmountAndDate() {
        t2.setPending(false);

        // Exact matches = result(1)
        FuzzyTransactionMatcher.Result result = FuzzyTransactionMatcher.compare(t1, t2, provider, true);
        assertEquals(1, result.getScore(), 0);

        // Description differs slightly (ICA nära HUDDINGE > ICA nära) = result(0.963)
        t2.setOriginalDescription(t1.getOriginalDescription().substring(0, t1.getOriginalDescription().length() / 2));
        result = FuzzyTransactionMatcher.compare(t1, t2, provider, true);

        assertEquals(0.963, result.getScore(), 0.001);

        // Description differs slightly (16%) = result(0.9045)
        t2.setOriginalDescription(t1.getOriginalDescription());
        t2.setOriginalAmount(t1.getOriginalAmount() * 1.16);

        result = FuzzyTransactionMatcher.compare(t1, t2, provider, true);
        assertEquals(0.9045, result.getScore(), 0.0001);

        // Date differs slightly (5 days) = result(0.9873)
        t2.setOriginalAmount(t1.getOriginalAmount());
        t2.setOriginalDate(DateUtils.addDays(t1.getOriginalDate(), 5));

        result = FuzzyTransactionMatcher.compare(t1, t2, provider, true);
        assertEquals(0.9873, result.getScore(), 0.0001);
    }

    @Test
    public void ensureNoNpeIsThrown_whenPendingDescriptionPatternMap_doesNotContainPattern_forTheGivenProvider() {
        Mockito.when(provider.getGroupDisplayName()).thenReturn("TestProvider that does not exist");

        assertFalse(FuzzyTransactionMatcher.isPredefinedPendingDescription(provider, "prel"));
    }

    @Test
    public void ensureNullAndEmptyDescriptions_getMaxScore() {
        // Both are empty
        t1.setOriginalDescription("");
        t2.setOriginalDescription(t1.getOriginalDescription());

        FuzzyTransactionMatcher.Result result = FuzzyTransactionMatcher.compare(t1, t2, provider, true);

        Assert.assertEquals(1, result.getDescriptionScore(), 0);
        Assert.assertEquals(1, result.getScore(), 0);

        // 1 is empty and 1 is null
        t1.setOriginalDescription(null);

        result = FuzzyTransactionMatcher.compare(t1, t2, provider, true);

        Assert.assertEquals(1, result.getDescriptionScore(), 0);
        Assert.assertEquals(1, result.getScore(), 0);

        // Both are null
        t2.setOriginalDescription(null);

        result = FuzzyTransactionMatcher.compare(t1, t2, provider, true);

        Assert.assertEquals(1, result.getDescriptionScore(), 0);
        Assert.assertEquals(1, result.getScore(), 0);
    }

    @Test
    public void ensureNullOrEmptyDescription_comparedWithNonEmptyDescription_getMinScore() {
        t1.setOriginalDescription("");

        FuzzyTransactionMatcher.Result result = FuzzyTransactionMatcher.compare(t1, t2, provider, true);

        Assert.assertEquals(0, result.getDescriptionScore(), 0);
        Assert.assertEquals(0, result.getScore(), 0);

        t1.setOriginalDescription(null);

        result = FuzzyTransactionMatcher.compare(t1, t2, provider, true);

        Assert.assertEquals(0, result.getDescriptionScore(), 0);
        Assert.assertEquals(0, result.getScore(), 0);
    }

    @Test
    public void testPendingDescriptionRegexByProvider() {
        // Handelsbanken pending descriptions
        assertTrue(FuzzyTransactionMatcher.isPredefinedPendingDescription(provider, "Prel."));
        assertTrue(FuzzyTransactionMatcher.isPredefinedPendingDescription(provider, "Prel. KORTKÖP"));
        assertFalse(FuzzyTransactionMatcher.isPredefinedPendingDescription(provider, "Prel. "));
        assertTrue(FuzzyTransactionMatcher.isPredefinedPendingDescription(provider, "Prel"));
        assertFalse(FuzzyTransactionMatcher.isPredefinedPendingDescription(provider, "Prel "));

        // Does not match Länsförsäkringar pending description
        assertFalse(FuzzyTransactionMatcher.isPredefinedPendingDescription(provider, "Prel KORTKÖP"));

        // Does not match Swedbank & Sparbankerna pending descriptions
        assertFalse(FuzzyTransactionMatcher.isPredefinedPendingDescription(provider, "SKYDDAT BELOPP"));
        assertFalse(FuzzyTransactionMatcher.isPredefinedPendingDescription(provider, "ÖVF VIA INTERNET"));

        // Lansforsakringar pending descriptions
        Mockito.when(provider.getGroupDisplayName()).thenReturn(ProviderGroupName.LANSFORSAKRINGAR);

        assertTrue(FuzzyTransactionMatcher.isPredefinedPendingDescription(provider, "Prel"));
        assertTrue(FuzzyTransactionMatcher.isPredefinedPendingDescription(provider, "Prel KORTKÖP"));
        assertFalse(FuzzyTransactionMatcher.isPredefinedPendingDescription(provider, "Prel "));

        // Does not match Handelsbanken pending descriptions
        assertFalse(FuzzyTransactionMatcher.isPredefinedPendingDescription(provider, "Prel."));
        assertFalse(FuzzyTransactionMatcher.isPredefinedPendingDescription(provider, "Prel. KORTKÖP"));

        // Does not match Swedbank & Sparbankerna pending descriptions
        assertFalse(FuzzyTransactionMatcher.isPredefinedPendingDescription(provider, "SKYDDAT BELOPP"));
        assertFalse(FuzzyTransactionMatcher.isPredefinedPendingDescription(provider, "ÖVF VIA INTERNET"));

        // Swedbank pending descriptions
        Mockito.when(provider.getGroupDisplayName()).thenReturn(ProviderGroupName.SWEDBANK_SPARBANKERNA);

        assertTrue(FuzzyTransactionMatcher.isPredefinedPendingDescription(provider, "SKYDDAT BELOPP"));
        assertTrue(FuzzyTransactionMatcher.isPredefinedPendingDescription(provider, "ÖVF VIA INTERNET"));
        // Does not match Handelsbanken pending descriptions
        assertFalse(FuzzyTransactionMatcher.isPredefinedPendingDescription(provider, "Prel."));
        assertFalse(FuzzyTransactionMatcher.isPredefinedPendingDescription(provider, "Prel. KORTKÖP"));
        // Does not match Länsförsäkringar pending description
        assertFalse(FuzzyTransactionMatcher.isPredefinedPendingDescription(provider, "Prel"));
        assertFalse(FuzzyTransactionMatcher.isPredefinedPendingDescription(provider, "Prel KORTKÖP"));
    }

    private void addExternalIdTo(Transaction transaction, String externalId) {
        transaction.setPayload(TransactionPayloadTypes.EXTERNAL_ID, externalId);
    }

    @Test
    public void testIsSignDifferent() {
        assertFalse(FuzzyTransactionMatcher.isSignDifferent(Double.MAX_VALUE, Double.MAX_VALUE));
        assertFalse(FuzzyTransactionMatcher.isSignDifferent(-Double.MAX_VALUE, -Double.MAX_VALUE));
        assertTrue(FuzzyTransactionMatcher.isSignDifferent(Double.MAX_VALUE, -Double.MAX_VALUE));
        assertTrue(FuzzyTransactionMatcher.isSignDifferent(-Double.MAX_VALUE, Double.MAX_VALUE));
        assertFalse(FuzzyTransactionMatcher.isSignDifferent(10, 10));
        assertFalse(FuzzyTransactionMatcher.isSignDifferent(-10, -10));
        assertTrue(FuzzyTransactionMatcher.isSignDifferent(10, -10));
        assertTrue(FuzzyTransactionMatcher.isSignDifferent(-10, 10));
        assertFalse(FuzzyTransactionMatcher.isSignDifferent(0, 10));
        assertFalse(FuzzyTransactionMatcher.isSignDifferent(10, 0));
        assertFalse(FuzzyTransactionMatcher.isSignDifferent(0, -10));
        assertFalse(FuzzyTransactionMatcher.isSignDifferent(-10, 0));
        assertFalse(FuzzyTransactionMatcher.isSignDifferent(0, 0));
    }
}
