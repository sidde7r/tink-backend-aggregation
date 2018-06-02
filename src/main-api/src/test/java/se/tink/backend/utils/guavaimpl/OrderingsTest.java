package se.tink.backend.utils.guavaimpl;

import org.joda.time.DateTime;
import org.junit.Test;
import se.tink.backend.core.Transaction;
import static org.assertj.core.api.Assertions.assertThat;

public class OrderingsTest {

    @Test
    public void testOriginalDateSorting() {

        Transaction left = new Transaction();
        Transaction right = new Transaction();

        left.setOriginalDate(new DateTime(2016, 1, 1, 0, 0).toDate());
        right.setOriginalDate(new DateTime(2016, 1, 2, 0, 0).toDate());

        assertThat(Orderings.TRANSACTION_ORIGINAL_DATE_ORDERING.compare(left, right)).isLessThan(0);
    }

    @Test
    public void testOriginalDateSortingWithSameDate() {

        Transaction left = new Transaction();
        Transaction right = new Transaction();

        left.setOriginalDate(new DateTime(2016, 1, 1, 0, 0).toDate());
        right.setOriginalDate(new DateTime(2016, 1, 1, 0, 0).toDate());

        left.setOriginalDate(new DateTime(2017, 1, 3, 0, 0).toDate());
        right.setOriginalDate(new DateTime(2076, 1, 4, 0, 0).toDate());

        assertThat(Orderings.TRANSACTION_ORIGINAL_DATE_ORDERING.compare(left, right)).isLessThan(0);
    }

}
