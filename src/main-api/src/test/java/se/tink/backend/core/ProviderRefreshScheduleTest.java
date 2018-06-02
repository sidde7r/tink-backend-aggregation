package se.tink.backend.core;

import org.joda.time.LocalTime;
import org.junit.Test;
import se.tink.libraries.serialization.utils.SerializationUtils;
import static org.assertj.core.api.Assertions.assertThat;

public class ProviderRefreshScheduleTest {

    @Test
    public void testIsActiveAtWithoutMidnightOverlap() {
        ProviderRefreshSchedule refreshSchedule = new ProviderRefreshSchedule("02:00", "07:00");

        assertThat(refreshSchedule.isActiveAt(LocalTime.parse("01:59:59"))).isFalse();
        assertThat(refreshSchedule.isActiveAt(LocalTime.parse("02:00:00"))).isTrue();
        assertThat(refreshSchedule.isActiveAt(LocalTime.parse("03:00:00"))).isTrue();
        assertThat(refreshSchedule.isActiveAt(LocalTime.parse("06:59:59"))).isTrue();
        assertThat(refreshSchedule.isActiveAt(LocalTime.parse("07:00:00"))).isFalse();
    }

    @Test
    public void testIsActiveAtWithMidnightOverlap() {
        ProviderRefreshSchedule refreshSchedule = new ProviderRefreshSchedule("23:00", "03:00");

        assertThat(refreshSchedule.isActiveAt(LocalTime.parse("22:59:59"))).isFalse();
        assertThat(refreshSchedule.isActiveAt(LocalTime.parse("23:00:00"))).isTrue();
        assertThat(refreshSchedule.isActiveAt(LocalTime.parse("00:00:00"))).isTrue();
        assertThat(refreshSchedule.isActiveAt(LocalTime.parse("02:59:59"))).isTrue();
        assertThat(refreshSchedule.isActiveAt(LocalTime.parse("03:00:00"))).isFalse();
    }

    @Test(expected = IllegalStateException.class)
    public void testSameFromAndTo() {
        ProviderRefreshSchedule refreshSchedule = new ProviderRefreshSchedule("23:00", "23:00");
    }

    @Test
    public void testSerialization() {
        ProviderRefreshSchedule refreshSchedule = new ProviderRefreshSchedule("10:10", "13:20");

        String serialized = SerializationUtils.serializeToString(refreshSchedule);

        ProviderRefreshSchedule refreshScheduleNew = SerializationUtils
                .deserializeFromString(serialized, ProviderRefreshSchedule.class);

        assertThat(refreshScheduleNew).isNotNull();
        assertThat(refreshScheduleNew.getFromString()).isEqualTo(refreshSchedule.getFromString());
        assertThat(refreshScheduleNew.getToAsString()).isEqualTo(refreshSchedule.getToAsString());
    }
}
