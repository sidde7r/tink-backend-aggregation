package se.tink.backend.common.utils;

import java.net.URISyntaxException;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class DeepLinkBuilderFactoryTest {

    private final DeepLinkBuilderFactory deepLinkBuilderFactory;

    public DeepLinkBuilderFactoryTest() {
        deepLinkBuilderFactory = new DeepLinkBuilderFactory("tink://");
    }

    @Test
    public void leftToSpendLink_periodAndSource_isCorrect() throws URISyntaxException {
        String period = "testPeriod";
        String source = "testSource";

        assertEquals("tink://left-to-spend/?period=testPeriod&source=testSource",
                deepLinkBuilderFactory.leftToSpend().withPeriod(period).withSource(source).build());

        assertEquals("tink://left-to-spend", deepLinkBuilderFactory.leftToSpend().build());

        assertEquals("tink://left-to-spend/?period=testPeriod",
                deepLinkBuilderFactory.leftToSpend().withPeriod(period).build());

        assertEquals("tink://left-to-spend/?source=testSource",
                deepLinkBuilderFactory.leftToSpend().withSource(source).build());
    }

    @Test
    public void manualRefreshReminder_sourceMediumAndCampaign_isCorrect() {
        assertEquals("tink://open/?source=tink&medium=notification&campaign=manual-refresh-reminder-15",
                deepLinkBuilderFactory.manualRefreshReminder(15).build());
    }
}
