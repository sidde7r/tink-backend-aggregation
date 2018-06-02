package se.tink.backend.common.workers.activity.generators;

import org.junit.Test;
import se.tink.backend.common.workers.activity.generators.models.WeeklySummaryActivityData;
import se.tink.backend.core.Activity;
import se.tink.backend.core.KVPair;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertNotNull;

public class WeeklySummaryActivityDataTest {

    @Test
    public void weeklySummaryContentSerialization() {
        final WeeklySummaryActivityData weeklySummaryActivityData = new WeeklySummaryActivityData();

        weeklySummaryActivityData.setHistoricalExpenses(asList(
                new KVPair<>("a", 42.),
                new KVPair<>("b", 5.3)));
        final Activity activity = new Activity();
        activity.setContent(weeklySummaryActivityData);

        final WeeklySummaryActivityData content = activity.getContent(WeeklySummaryActivityData.class);
        assertNotNull(content);
    }
}
