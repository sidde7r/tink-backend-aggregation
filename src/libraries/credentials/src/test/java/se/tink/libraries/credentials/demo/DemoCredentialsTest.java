package se.tink.libraries.credentials.demo;

import org.junit.Assert;
import org.junit.Test;
import static org.assertj.core.api.Assertions.assertThat;

public class DemoCredentialsTest {
    @Test
    public void testHasFeature() throws Exception {
        Assert.assertFalse(DemoCredentials.USER1.hasFeature(DemoCredentials.DemoUserFeature.RANDOMIZE_TRANSACTIONS));
        Assert.assertTrue(DemoCredentials.USER5.hasFeature(DemoCredentials.DemoUserFeature.RANDOMIZE_TRANSACTIONS));
    }

    @Test
    public void shouldIncludeAllDemoUsersWhenCheckingIfIsDemoUser() {
        for (DemoCredentials user : DemoCredentials.values()) {
            String username = user.getUsername();
            assertThat(DemoCredentials.isDemoUser(username)).isTrue();
        }
    }

    @Test
    public void aNonExistingDemoUserIsNotADemoUser() {
        String notExisting = "198607015537";
        assertThat(DemoCredentials.isDemoUser(notExisting)).isFalse();
    }

    @Test
    public void nullAndEmptyAreNotADemoUsers() {
        assertThat(DemoCredentials.isDemoUser(null)).isFalse();
        assertThat(DemoCredentials.isDemoUser("")).isFalse();
    }
}
