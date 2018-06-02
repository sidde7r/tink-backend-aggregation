package se.tink.backend.common.config;

import org.junit.Test;
import static org.assertj.core.api.Assertions.assertThat;

public class NotificationsConfigurationTest {

    @Test
    public void shouldAlwaysSendNotificationIfWholeDayIsConfigured() throws Exception {

        NotificationsConfiguration configuration = new NotificationsConfiguration();

        configuration.setStartHourOfDay(0);
        configuration.setEndHourOfDay(24);

        // Check that we always send out notifications
        for (int i = 0; i < 24; i++){
            assertThat(configuration.shouldSendNotifications(i)).isTrue();
        }
    }
}
