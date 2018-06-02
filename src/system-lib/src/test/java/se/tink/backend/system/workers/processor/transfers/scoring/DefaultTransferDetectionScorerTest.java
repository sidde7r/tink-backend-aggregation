package se.tink.backend.system.workers.processor.transfers.scoring;

import org.joda.time.DateTime;
import org.junit.Test;
import se.tink.backend.core.Transaction;
import static org.assertj.core.api.Assertions.assertThat;

public class DefaultTransferDetectionScorerTest {

    @Test
    public void testTransactionsOnSameDay() {
        Transaction left = createTransaction(DateTime.now(), "A");
        Transaction right = createTransaction(DateTime.now(), "B");

        DefaultTransferDetectionScorer scorer = new DefaultTransferDetectionScorer();

        // Should get score 3 since the days are the same and there is 0 similarity between descriptions
        assertThat(scorer.getScore(left, right)).isEqualTo(3);
    }

    @Test
    public void testTransactionsOnDifferentDays() {
        Transaction left = createTransaction(DateTime.now(), "A");
        Transaction right = createTransaction(DateTime.now().plusDays(1), "B");

        DefaultTransferDetectionScorer scorer = new DefaultTransferDetectionScorer();

        // Should get score 2 since there is one day diff and 0 similarity between descriptions
        assertThat(scorer.getScore(left, right)).isEqualTo(2);
    }

    @Test
    public void testTransactionsWithSimiliarDescription() {
        Transaction left = createTransaction(DateTime.now(), "A");
        Transaction right = createTransaction(DateTime.now(), "A");

        DefaultTransferDetectionScorer scorer = new DefaultTransferDetectionScorer();

        // Should get score 4 since the days are the same and the description is the same
        assertThat(scorer.getScore(left, right)).isEqualTo(4);
    }

    private Transaction createTransaction(DateTime dateTime, String description) {
        Transaction transaction = new Transaction();
        transaction.setOriginalDate(dateTime.toDate());
        transaction.setOriginalDescription(description);

        return transaction;
    }

}
