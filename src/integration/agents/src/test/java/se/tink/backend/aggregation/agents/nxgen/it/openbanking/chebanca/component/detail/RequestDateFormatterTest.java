package se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.component.detail;

import static org.junit.Assert.assertEquals;

import java.time.LocalDateTime;
import java.util.Arrays;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.detail.RequestDateFormatter;

@RunWith(Parameterized.class)
public class RequestDateFormatterTest {
    private LocalDateTime givenDate;
    private String expectedDateAsString;

    public RequestDateFormatterTest(LocalDateTime givenDate, String expectedDateAsString) {
        this.givenDate = givenDate;
        this.expectedDateAsString = expectedDateAsString;
    }

    @Test
    public void shouldReturnRequestDateFormattedProperly() {
        // when
        String dateAsString = RequestDateFormatter.getDateFormatted(givenDate);

        // then
        assertEquals(expectedDateAsString, dateAsString);
    }

    @Parameterized.Parameters(name = "{index}: Test with date={0}, result: {1}")
    public static Iterable<Object[]> data() {
        return Arrays.asList(
                new Object[][] {
                    {LocalDateTime.of(2019, 12, 11, 13, 21, 5), "Wed, 11 Dec 2019 01:21:05 GMT"},
                    {LocalDateTime.of(2020, 9, 1, 1, 1, 54), "Tue, 01 Sep 2020 01:01:54 GMT"},
                    {LocalDateTime.of(2020, 2, 28, 20, 59, 59), "Fri, 28 Feb 2020 08:59:59 GMT"}
                });
    }
}
