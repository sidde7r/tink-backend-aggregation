package se.tink.backend.aggregation.events;

import com.fasterxml.jackson.databind.node.JsonNodeType;
import java.util.Collections;
import org.junit.Assert;
import org.junit.Test;
import se.tink.backend.aggregation.nxgen.http.event.type_detection.DateFieldTypeDetectionStrategy;

public class DateFieldTypeDetectionStrategyTest {

    // Tests for valid dates (Year, Month, Day)
    @Test
    public void validDateShouldBeDetected1() {
        Assert.assertTrue(
                new DateFieldTypeDetectionStrategy()
                        .isTypeMatched(Collections.emptyList(), "1992-01-13", JsonNodeType.STRING));
    }

    @Test
    public void validDateShouldBeDetected2() {
        Assert.assertTrue(
                new DateFieldTypeDetectionStrategy()
                        .isTypeMatched(Collections.emptyList(), "2092/01/13", JsonNodeType.STRING));
    }

    @Test
    public void validDateShouldBeDetected3() {
        Assert.assertTrue(
                new DateFieldTypeDetectionStrategy()
                        .isTypeMatched(Collections.emptyList(), "13/01/1992", JsonNodeType.STRING));
    }

    // Tests for valid dates with timestamp

    @Test
    public void validDateShouldBeDetected4() {
        Assert.assertTrue(
                new DateFieldTypeDetectionStrategy()
                        .isTypeMatched(
                                Collections.emptyList(),
                                "2020-11-17T00:00:00",
                                JsonNodeType.STRING));
    }

    @Test
    public void validDateShouldBeDetected5() {
        Assert.assertTrue(
                new DateFieldTypeDetectionStrategy()
                        .isTypeMatched(
                                Collections.emptyList(),
                                "2020-09-24T00:00:00.000+0200",
                                JsonNodeType.STRING));
    }

    @Test
    public void validDateShouldBeDetected6() {
        Assert.assertTrue(
                new DateFieldTypeDetectionStrategy()
                        .isTypeMatched(
                                Collections.emptyList(),
                                "2020-10-23T07:58:50Z",
                                JsonNodeType.STRING));
    }

    // Tests for invalid data
    @Test
    public void invalidDateShouldBeDetected1() {
        Assert.assertFalse(
                new DateFieldTypeDetectionStrategy()
                        .isTypeMatched(Collections.emptyList(), "13011992", JsonNodeType.STRING));
    }

    @Test
    public void invalidDateShouldBeDetected2() {
        Assert.assertFalse(
                new DateFieldTypeDetectionStrategy()
                        .isTypeMatched(
                                Collections.emptyList(),
                                "13/01/1992dummy-suffix",
                                JsonNodeType.STRING));
    }
}
