package se.tink.backend.aggregation.agents.nxgen.nl.banks.ics;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.configuration.ICSConfiguration;

public class ICSConfigurationTest {

    private ICSConfiguration icsConfiguration;

    @Test
    public void shouldReturnFalseWhenConfigurationIsMissing() {
        // given
        icsConfiguration = mock(ICSConfiguration.class);

        // then
        assertFalse(icsConfiguration.isValid());
    }

    @Test
    public void shouldReturnTrueIfConfigurationIsPresent() {
        // given
        icsConfiguration = TestHelper.getIcsConfiguration();

        // then
        assertTrue(icsConfiguration.isValid());
    }

    @Test
    public void shouldReturnFalseIfClientSecretIsMissing() {
        // given
        icsConfiguration = TestHelper.getIcsConfigurationWithoutClientSecret();

        // then
        assertFalse(icsConfiguration.isValid());
    }

    @Test
    public void shouldReturnFalseIfClientIdIsMissing() {
        // given
        icsConfiguration = TestHelper.getIcsConfigurationWithoutClientId();

        // then
        assertFalse(icsConfiguration.isValid());
    }
}
