package se.tink.backend.aggregation.agents.banks.crosskey.utils;

import com.google.common.collect.Lists;
import com.google.common.collect.Range;
import org.joda.time.DateTime;
import org.junit.Test;
import se.tink.backend.aggregation.agents.models.Transaction;
import static org.assertj.core.api.Assertions.assertThat;

public class CrossKeyUtilsTest {
    @Test(expected = IllegalArgumentException.class)
    public void getDateRangeExpectsNonEmptyList() {
        CrossKeyUtils.getDateRange(Lists.<Transaction>newArrayList());
    }

    @Test
    public void getDateRangeOfSingleTransactionList() {
        Range<DateTime> dateRange = CrossKeyUtils.getDateRange(Lists.newArrayList(
                createTransaction(2017, 2, 25)));

        assertThat(dateRange.lowerEndpoint())
                .isEqualTo(dateRange.upperEndpoint())
                .isEqualTo(createDate(2017,2, 25));
    }

    @Test
    public void getDateRangeOfMultiTransactionListWihtoutOrder() {
        Range<DateTime> dateRange = CrossKeyUtils.getDateRange(Lists.newArrayList(
                createTransaction(2017, 2, 25),
                createTransaction(2017, 3, 10),
                createTransaction(2015, 4, 30), // <-- Min
                createTransaction(2018, 2, 12))); // <-- Max

        assertThat(dateRange.lowerEndpoint()).isEqualTo(createDate(2015, 4, 30));
        assertThat(dateRange.upperEndpoint()).isEqualTo(createDate(2018, 2, 12));
    }

    @Test
    public void getFirstPageOverYearBoundary() {
        Range<DateTime> firstPage = CrossKeyUtils.getFirstPage(createDate(2017, 1, 31));

        assertThat(firstPage.upperEndpoint()).isEqualTo(createDate(2017, 1, 31));
        assertThat(firstPage.lowerEndpoint()).isEqualTo(createDate(2016, 12, 1));
    }

    @Test
    public void getFirstPageForEndOfMarch() {
        Range<DateTime> firstPage = CrossKeyUtils.getFirstPage(createDate(2017, 3, 31));

        assertThat(firstPage.upperEndpoint()).isEqualTo(createDate(2017, 3, 31));
        assertThat(firstPage.lowerEndpoint()).isEqualTo(createDate(2017, 2, 1));
    }

    @Test
    public void getFirstPageForMidApril() {
        Range<DateTime> firstPage = CrossKeyUtils.getFirstPage(createDate(2017, 4, 15));

        assertThat(firstPage.upperEndpoint()).isEqualTo(createDate(2017, 4, 15));
        assertThat(firstPage.lowerEndpoint()).isEqualTo(createDate(2017, 3, 1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void getNextPageExpectsPreviousLowerBoundToBeTheFirstOfAMonth() {
        CrossKeyUtils.getNextPage(createDate(2017, 2, 10));
    }

    @Test
    public void getNextPageOverYearBoundary() {
        Range<DateTime> firstPage = CrossKeyUtils.getNextPage(createDate(2017, 2, 1));

        assertThat(firstPage.upperEndpoint()).isEqualTo(createDate(2017, 1, 31));
        assertThat(firstPage.lowerEndpoint()).isEqualTo(createDate(2016, 12, 1));
    }

    private static Transaction createTransaction(int year, int monthOfYear, int dayOfMonth) {
        Transaction t = new Transaction();
        t.setDate(createDate(year, monthOfYear, dayOfMonth).toDate());

        return t;
    }

    private static DateTime createDate(int year, int monthOfYear, int dayOfMonth) {
        return new DateTime(year, monthOfYear, dayOfMonth, 0, 0);
    }
}
