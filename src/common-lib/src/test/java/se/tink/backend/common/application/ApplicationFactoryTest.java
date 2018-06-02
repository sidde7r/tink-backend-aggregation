package se.tink.backend.common.application;

import org.joda.time.DateTime;
import org.junit.Test;
import se.tink.backend.core.property.Property;
import static org.assertj.core.api.Assertions.assertThat;

public class ApplicationFactoryTest {

    @Test
    public void testPropertyByDate() {

        Property p1 = new Property();
        Property p2 = new Property();

        // Test `null` safety
        assertThat(ApplicationFactory.PROPERTY_BY_DATE_ASC.compare(p1, p2)).isEqualTo(0);

        p1.setCreated(new DateTime(2016, 1, 1, 0, 0).toDate());

        // Test that `null` is intepreted as lower
        assertThat(ApplicationFactory.PROPERTY_BY_DATE_ASC.compare(p1, p2)).isGreaterThan(0);

        p2.setCreated(new DateTime(2016, 1, 1, 0, 0).toDate());

        // Test comparison stability (same date; first object first)
        assertThat(ApplicationFactory.PROPERTY_BY_DATE_ASC.compare(p1, p2)).isEqualTo(0);

        p2.setCreated(new DateTime(2016, 2, 1, 0, 0).toDate());

        // Test comparison of different dates
        assertThat(ApplicationFactory.PROPERTY_BY_DATE_ASC.compare(p1, p2)).isLessThan(0);
    }

}
