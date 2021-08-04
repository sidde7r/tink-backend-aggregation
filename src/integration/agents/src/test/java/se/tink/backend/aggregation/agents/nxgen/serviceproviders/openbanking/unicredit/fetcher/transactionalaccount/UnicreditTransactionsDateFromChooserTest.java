package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.fetcher.transactionalaccount;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.Period;
import java.util.Optional;
import java.util.function.Function;
import org.junit.Test;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.ConstantLocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;

public class UnicreditTransactionsDateFromChooserTest {

    private static final LocalDateTimeSource LOCAL_DATE_TIME_SOURCE =
            new ConstantLocalDateTimeSource();
    private static final LocalDate LAST_TRANSACTIONS_DATE_FETCHED =
            LOCAL_DATE_TIME_SOURCE.now().minusDays(2).toLocalDate();

    @Test
    public void shouldGetDateFromWhenLastTransactionsDateFetchedIsHigher() {
        UnicreditTransactionsDateFromChooser unicreditTransactionsDateFromChooser =
                createUnicreditTransactionsDateFromChooser(
                        (firstFetch) -> LAST_TRANSACTIONS_DATE_FETCHED.minusDays(1));

        Optional<LocalDate> lastTransactionsDateFetched =
                Optional.of(LAST_TRANSACTIONS_DATE_FETCHED);

        LocalDate dateFrom =
                unicreditTransactionsDateFromChooser.getDateFrom(lastTransactionsDateFetched);

        assertThat(dateFrom).isEqualTo(lastTransactionsDateFetched.get());
    }

    @Test
    public void shouldGetDateFromWhenLastTransactionsDateFetchedIsLower() {
        UnicreditTransactionsDateFromChooser unicreditTransactionsDateFromChooser =
                createUnicreditTransactionsDateFromChooser(
                        (firstFetch) -> LAST_TRANSACTIONS_DATE_FETCHED.plusDays(1));

        Optional<LocalDate> lastTransactionsDateFetched =
                Optional.of(LAST_TRANSACTIONS_DATE_FETCHED);

        LocalDate dateFrom =
                unicreditTransactionsDateFromChooser.getDateFrom(lastTransactionsDateFetched);

        assertThat(dateFrom).isEqualTo(LAST_TRANSACTIONS_DATE_FETCHED.plusDays(1));
    }

    @Test
    public void shouldGetDateFromWhenLastTransactionsDateFetchedIsNotPresent() {
        UnicreditTransactionsDateFromChooser unicreditTransactionsDateFromChooser =
                createUnicreditTransactionsDateFromChooser(
                        (firstFetch) -> LAST_TRANSACTIONS_DATE_FETCHED);

        Optional<LocalDate> lastTransactionsDateFetched = Optional.empty();

        LocalDate dateFrom =
                unicreditTransactionsDateFromChooser.getDateFrom(lastTransactionsDateFetched);

        assertThat(dateFrom).isEqualTo(LAST_TRANSACTIONS_DATE_FETCHED);
    }

    @Test
    public void shouldSubtractDays() {
        UnicreditTransactionsDateFromChooser unicreditTransactionsDateFromChooser =
                createUnicreditTransactionsDateFromChooser((firstFetch) -> null);
        LocalDate dateFrom = unicreditTransactionsDateFromChooser.subtract(Period.ofDays(2));
        assertThat(dateFrom).isEqualTo(LocalDate.of(1992, 4, 8));
    }

    @Test
    public void shouldSubtractYears() {
        UnicreditTransactionsDateFromChooser unicreditTransactionsDateFromChooser =
                createUnicreditTransactionsDateFromChooser((firstFetch) -> null);
        LocalDate dateFrom =
                unicreditTransactionsDateFromChooser.subtractYearsCountingCurrentAsOne(
                        Period.ofYears(2));
        assertThat(dateFrom).isEqualTo(LocalDate.of(1991, 1, 1));
    }

    private UnicreditTransactionsDateFromChooser createUnicreditTransactionsDateFromChooser(
            Function<Boolean, LocalDate> selectMinDateFromFunction) {
        return new UnicreditTransactionsDateFromChooser(LOCAL_DATE_TIME_SOURCE) {
            @Override
            protected LocalDate selectMinDateFrom(boolean firstFetch) {
                return selectMinDateFromFunction.apply(firstFetch);
            }
        };
    }
}
