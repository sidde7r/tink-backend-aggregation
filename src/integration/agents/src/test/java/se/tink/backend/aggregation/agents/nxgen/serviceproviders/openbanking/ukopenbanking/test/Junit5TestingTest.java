package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.test;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class Junit5TestingTest {
    // TODO: remove this class and its package. it's only for checking if junit5 works properly

    @Test
    void shouldReturn10() {
        Junit5Testing junit5Testing = new Junit5Testing();
        int expected = junit5Testing.doubleInteger(5);
        Assertions.assertThat(expected).isEqualTo(10);
    }
}
