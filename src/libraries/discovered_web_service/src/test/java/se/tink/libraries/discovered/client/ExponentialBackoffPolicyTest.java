package se.tink.libraries.discovered.client;

import org.junit.Assert;
import org.junit.Test;
import se.tink.libraries.discovered.ExponentialBackoffPolicy;

public class ExponentialBackoffPolicyTest {

    @Test
    public void testBasic() {
        ExponentialBackoffPolicy backoff = new ExponentialBackoffPolicy(1000);
        Assert.assertEquals(0, backoff.getSleepDuration());
        Assert.assertEquals(1000, backoff.getSleepDuration());
        Assert.assertEquals(2000, backoff.getSleepDuration());
        Assert.assertEquals(4000, backoff.getSleepDuration());
        Assert.assertEquals(8000, backoff.getSleepDuration());
    }
}
