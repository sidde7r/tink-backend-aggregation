package se.tink.backend.common.statistics.functions.lefttospendaverage.dto;

import java.text.ParseException;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Rule;
import org.junit.Test;
import se.tink.backend.common.SwedishTimeRule;
import se.tink.libraries.date.ResolutionTypes;
import se.tink.backend.core.Statistic;
import static org.assertj.core.api.Assertions.assertThat;

public class DateStatisticTest {

    @Rule
    public SwedishTimeRule timeRule = new SwedishTimeRule();

    @Test
    public void constructionWithStatisticHasExpectedValues() throws ParseException {
        Statistic statistic = stubStatistic("2015-02-28");

        DateStatistic dateStatistic = new DateStatistic(statistic);

        assertThat(dateStatistic.getDateTime()).isEqualTo(getDate("2015-02-28"));
        assertThat(dateStatistic.getStatistic()).isSameAs(statistic);
    }

    @Test
    public void copyConstructionHasExpectedValues() throws ParseException {
        Statistic statistic = stubStatistic("2015-02-28");
        DateStatistic dateStatistic = new DateStatistic(statistic);

        DateStatistic dateStatisticCopy = new DateStatistic(dateStatistic);

        assertThat(dateStatisticCopy.getDateTime()).isEqualTo(getDate("2015-02-28"));
        assertThat(dateStatisticCopy.getStatistic()).isNotSameAs(dateStatistic.getStatistic());
        assertThat(dateStatisticCopy.getStatistic()).isEqualTo(statistic);
    }

    @Test
    public void addConstructionHasExpectedValues() throws ParseException {
        Statistic statistic = stubStatistic("2015-02-28");
        DateStatistic dateStatistic = new DateStatistic(statistic);

        DateStatistic dateStatisticCopy = new DateStatistic(dateStatistic, 1);

        assertThat(dateStatisticCopy.getDateTime()).isEqualTo(getDate("2015-03-01"));

        assertThat(dateStatisticCopy.getStatistic()).isNotSameAs(dateStatistic.getStatistic());
        assertThat(dateStatisticCopy.getStatistic().getDescription()).isEqualTo("2015-03-01");

        // If we then just set the description back, the object should otherwise be equal to the first created one
        dateStatisticCopy.getStatistic().setDescription("2015-02-28");
        assertThat(dateStatisticCopy.getStatistic()).isEqualTo(statistic);
    }

    private static DateTime getDate(String date) {
        return DateTime.parse(date).withZoneRetainFields(DateTimeZone.UTC);
    }

    private static Statistic stubStatistic(String description) {
        Statistic statistic = new Statistic();

        statistic.setDescription(description);
        statistic.setPayload("A payload");
        statistic.setPeriod("2015-03");
        statistic.setResolution(ResolutionTypes.MONTHLY_ADJUSTED);
        statistic.setUserId("auserid");
        statistic.setValue(123.45);

        return statistic;
    }
}
