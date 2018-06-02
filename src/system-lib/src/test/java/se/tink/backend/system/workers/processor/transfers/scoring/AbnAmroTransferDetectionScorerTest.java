package se.tink.backend.system.workers.processor.transfers.scoring;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.joda.time.DateTime;
import org.junit.Test;
import se.tink.libraries.abnamro.utils.AbnAmroUtils;
import se.tink.backend.core.Account;
import se.tink.backend.core.Transaction;
import static org.assertj.core.api.Assertions.assertThat;

public class AbnAmroTransferDetectionScorerTest {

    @Test
    public void testTransactionsOnSameDayWithoutIban() {
        Transaction left = createTransaction(DateTime.now(), "A");
        Transaction right = createTransaction(DateTime.now(), "B");

        AbnAmroTransferDetectionScorer scorer = new AbnAmroTransferDetectionScorer(Lists.<Account>newArrayList());

        // Should get score 3 since the days are the same and there is 0 similarity between descriptions
        assertThat(scorer.getScore(left, right)).isEqualTo(3);
    }

    @Test
    public void testTransactionsOnSameDayWithDescriptionLinesWithOutIban() {
        Transaction left = createTransaction(DateTime.now(), "A");
        Transaction right = createTransaction(DateTime.now(), "B");

        left.setInternalPayload(AbnAmroUtils.InternalPayloadKeys.DESCRIPTION_LINES, "[\"Naam: erik\"]");

        AbnAmroTransferDetectionScorer scorer = new AbnAmroTransferDetectionScorer(Lists.<Account>newArrayList());

        // Should get score 3 since the days are the same and there is 0 similarity between descriptions
        assertThat(scorer.getScore(left, right)).isEqualTo(3);
    }

    @Test
    public void testTransactionsOnSameDayWithIban() {

        Account destinationAccount = new Account();
        destinationAccount.setPayload("{\"iban\":\"SE0101\"}");

        Transaction source = createTransaction(DateTime.now(), "A");
        Transaction destination = createTransaction(DateTime.now(), "A");

        destination.setAccountId(destinationAccount.getId());

        // Transfer money from source => destination. Source transaction will then have a field with iban number
        // of destination
        source.setInternalPayload(AbnAmroUtils.InternalPayloadKeys.DESCRIPTION_LINES, "[\"iban: SE0101\"]");

        ImmutableList<Account> accounts = ImmutableList.of(destinationAccount);

        AbnAmroTransferDetectionScorer scorer = new AbnAmroTransferDetectionScorer(accounts);

        // Should get score 9 since the days are the same day (score 3), same description (score 1) and it is from
        // source to destination (score 5)
        assertThat(scorer.getScore(source, destination)).isEqualTo(9);
    }

    private Transaction createTransaction(DateTime dateTime, String description) {
        Transaction transaction = new Transaction();
        transaction.setOriginalDate(dateTime.toDate());
        transaction.setOriginalDescription(description);

        return transaction;
    }
}
